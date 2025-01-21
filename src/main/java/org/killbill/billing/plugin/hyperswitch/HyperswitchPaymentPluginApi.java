/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014-2020 Groupon, Inc
 * Copyright 2020-2020 Equinix, Inc
 * Copyright 2014-2020 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.hyperswitch;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.*;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hyperswitch.client.auth.ApiKeyAuth;
import com.hyperswitch.client.model.*;
import org.apache.commons.codec.binary.Hex;
import org.joda.time.DateTime;
import org.killbill.billing.account.api.Account;
import org.killbill.billing.account.api.AccountApiException;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.osgi.libs.killbill.OSGIConfigPropertiesService;
import org.killbill.billing.osgi.libs.killbill.OSGIKillbillAPI;
import org.killbill.billing.payment.api.*;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.GatewayNotification;
import org.killbill.billing.payment.plugin.api.HostedPaymentPageFormDescriptor;
import org.killbill.billing.payment.plugin.api.PaymentMethodInfoPlugin;
import org.killbill.billing.payment.plugin.api.PaymentPluginApiException;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.core.PaymentApiWrapper;
import org.killbill.billing.plugin.api.payment.PluginPaymentMethodPlugin;
import org.killbill.billing.plugin.api.payment.PluginPaymentPluginApi;
import org.killbill.billing.plugin.hyperswitch.dao.HyperswitchDao;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchPaymentMethods;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchResponses;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records.HyperswitchPaymentMethodsRecord;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records.HyperswitchResponsesRecord;
import org.killbill.billing.plugin.util.KillBillMoney;
import org.killbill.billing.util.callcontext.CallContext;
import org.killbill.billing.util.callcontext.TenantContext;
import org.killbill.billing.util.entity.Pagination;
import org.killbill.clock.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperswitch.client.api.PaymentMethodsApi;
import com.hyperswitch.client.api.PaymentsApi;
import com.hyperswitch.client.api.RefundsApi;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

//
// A 'real' payment plugin would of course implement this interface.
//
public class HyperswitchPaymentPluginApi extends
    PluginPaymentPluginApi<HyperswitchResponsesRecord, HyperswitchResponses, HyperswitchPaymentMethodsRecord, HyperswitchPaymentMethods> {

    private static final Logger logger = LoggerFactory.getLogger(HyperswitchPaymentPluginApi.class);
    private final HyperswitchConfigurationHandler hyperswitchConfigurationHandler;
    private final HyperswitchDao hyperswitchDao;
    private final ObjectMapper objectMapper;

    // Cache map structure: tenantId -> apiClass -> client instance
    private final Map<UUID, Map<Class<?>, Object>> clientCache = new HashMap<>();

    public HyperswitchPaymentPluginApi(
        final HyperswitchConfigurationHandler hyperswitchConfigPropertiesConfigurationHandler,
        final OSGIKillbillAPI killbillAPI,
        final OSGIConfigPropertiesService configProperties,
        final Clock clock,
        final HyperswitchDao dao) {
        super(killbillAPI, configProperties, clock, dao);
        this.hyperswitchConfigurationHandler = hyperswitchConfigPropertiesConfigurationHandler;
        this.hyperswitchDao = dao;
        this.objectMapper = new ObjectMapper();
        this.objectMapper
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public PaymentTransactionInfoPlugin authorizePayment(final UUID kbAccountId,
                                                         final UUID kbPaymentId,
                                                         final UUID kbTransactionId,
                                                         final UUID kbPaymentMethodId,
                                                         final BigDecimal amount,
                                                         final Currency currency,
                                                         final Iterable<PluginProperty> properties,
                                                         final CallContext context) throws PaymentPluginApiException {
        try {
            // Check for existing payment ID and expiry
            HyperswitchResponsesRecord existingResponse = null;
            try {
                existingResponse = this.hyperswitchDao.getSuccessfulResponse(kbPaymentId, context.getTenantId());
            } catch (SQLException e) {
                logger.warn("Failed to retrieve existing response", e);
            }

            boolean shouldCreateNewPayment = true;
            if (existingResponse != null) {
                Map additionalData = HyperswitchDao.mapFromAdditionalDataString(existingResponse.getAdditionalData());
                if (additionalData.containsKey("expires_on")) {
                    DateTime expiresOn = DateTime.parse((String) additionalData.get("expires_on"));
                    if (expiresOn.isAfterNow()) {
                        shouldCreateNewPayment = false;
                    }
                }
            }

            // Build payment request for authorization
            PaymentsCreateRequest paymentsCreateRequest = new PaymentsCreateRequest();
            //paymentsCreateRequest.setAmount(KillBillMoney.toMinorUnits(currency.toString(), amount));
            paymentsCreateRequest.setAmount(0L);
            paymentsCreateRequest.setCurrency(convertCurrency(currency));
            paymentsCreateRequest.confirm(false);
            paymentsCreateRequest.customerId(kbAccountId.toString());
            paymentsCreateRequest.offSession(true);
            paymentsCreateRequest.profileId(hyperswitchConfigurationHandler.getConfigurable(context.getTenantId()).getProfileId());
            paymentsCreateRequest.setCaptureMethod(CaptureMethod.MANUAL);
            paymentsCreateRequest.setupFutureUsage(FutureUsage.OFF_SESSION);

            // Only create new payment if needed
            HyperswitchResponsesRecord hyperswitchRecord;
            PaymentsResponse response;
            if (shouldCreateNewPayment) {
                PaymentsApi clientApi = buildHyperswitchClient(context);
                try {
                    response = clientApi.createAPayment(paymentsCreateRequest);
                    response.setAmount(KillBillMoney.toMinorUnits(currency.toString(), amount));
                    // Only store new responses
                    hyperswitchRecord = storePaymentResponse(
                        kbAccountId,
                        kbPaymentId,
                        kbTransactionId,
                        kbPaymentMethodId,
                        amount,
                        currency,
                        response,
                        context.getTenantId()
                    );
                } catch (com.hyperswitch.client.ApiException e) {
                    throw new PaymentPluginApiException("Payment execution failed: " + e.getMessage(), e);
                }
            } else {
                // Just use the existing record directly
                hyperswitchRecord = existingResponse;
                response = new PaymentsResponse();
                response.setPaymentId(existingResponse.getPaymentAttemptId());
                response.setClientSecret(getClientSecretFromResponse(existingResponse));
                response.setStatus(getStatusFromResponse(existingResponse));
                response.setErrorMessage(existingResponse.getErrorMessage());
                response.setErrorCode(existingResponse.getErrorCode());
            }

            // Process response
            PaymentPluginStatus paymentPluginStatus = convertPaymentStatus(response.getStatus());
            // Update payment method with client secret
            if (response.getClientSecret() != null) {
                try {
                    this.hyperswitchDao.updatePaymentMethodClientSecret(
                        kbPaymentMethodId,
                        response.getClientSecret(),
                        context.getTenantId());
                } catch (SQLException e) {
                    logger.warn("Failed to update payment method client secret", e);
                }
            }

            // Build properties for frontend
            List<PluginProperty> paymentProperties = new ArrayList<>();
            paymentProperties.add(new PluginProperty("client_secret", response.getClientSecret(), false));
            paymentProperties.add(new PluginProperty("payment_id", response.getPaymentId(), false));
            if (response.getNextAction() != null) {
                paymentProperties.add(new PluginProperty("next_action", response.getNextAction(), false));
            }
            if (response.getReturnUrl() != null) {
                paymentProperties.add(new PluginProperty("return_url", response.getReturnUrl(), false));
            }

            return new HyperswitchPaymentTransactionInfoPlugin(
                hyperswitchRecord,
                kbPaymentId,
                kbTransactionId,
                TransactionType.AUTHORIZE,
                amount,
                currency,
                paymentPluginStatus,
                response.getErrorMessage(),
                response.getErrorCode(),
                response.getPaymentId(),
                response.getReferenceId(),
                DateTime.now(),
                DateTime.now(),
                paymentProperties
            );

        } catch (SQLException e) {
            throw new PaymentPluginApiException("Database error while processing payment", e);
        }
    }

    private String getClientSecretFromResponse(HyperswitchResponsesRecord response) {
        try {
            Map additionalData = HyperswitchDao.mapFromAdditionalDataString(response.getAdditionalData());
            return (String) additionalData.get("client_secret");
        } catch (Exception e) {
            logger.warn("Failed to extract client secret from response", e);
            return null;
        }
    }

    private IntentStatus getStatusFromResponse(HyperswitchResponsesRecord response) {
        try {
            Map additionalData = HyperswitchDao.mapFromAdditionalDataString(response.getAdditionalData());
            return IntentStatus.fromValue((String) additionalData.get("status"));
        } catch (Exception e) {
            logger.warn("Failed to extract status from response", e);
            return IntentStatus.REQUIRES_PAYMENT_METHOD;
        }
    }

    private HyperswitchResponsesRecord storePaymentResponse(UUID kbAccountId,
                                                            UUID kbPaymentId,
                                                            UUID kbTransactionId,
                                                            UUID kbPaymentMethodId,
                                                            BigDecimal amount,
                                                            Currency currency,
                                                            PaymentsResponse response,
                                                            UUID tenantId) throws SQLException {
        return this.hyperswitchDao.addResponse(
            kbAccountId,
            kbPaymentId,
            kbTransactionId,
            kbPaymentMethodId,
            TransactionType.AUTHORIZE,
            amount,
            currency,
            response,
            clock.getUTCNow(),
            tenantId
        );
    }

    @Override
    public PaymentTransactionInfoPlugin capturePayment(final UUID kbAccountId, final UUID kbPaymentId,
                                                       final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency,
                                                       final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        HyperswitchResponsesRecord hyperswitchRecord = null;
        try {
            hyperswitchRecord = this.hyperswitchDao.getSuccessfulResponse(kbPaymentId, context.getTenantId());
            PaymentTransactionInfoPlugin validationResult = this.refundValidations(hyperswitchRecord, amount);
            if (validationResult != null && validationResult.getStatus() == PaymentPluginStatus.CANCELED) {
                return validationResult;
            }
        } catch (SQLException e) {
            logger.error("[capturePayment]  but we encountered a database error", e);
            return HyperswitchPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
                TransactionType.CAPTURE, "[capturePayment] but we encountered a database error");
        }
        PaymentPluginStatus paymentPluginStatus = null;
        String payment_id = hyperswitchRecord.getPaymentAttemptId();
        PaymentsCaptureRequest paymentsRequest = new PaymentsCaptureRequest();
        Long amountToCapture = KillBillMoney.toMinorUnits(hyperswitchRecord.getCurrency(), amount);
        paymentsRequest.setAmountToCapture(amountToCapture);
        PaymentsApi ClientApi = buildHyperswitchClient(context);
        PaymentsResponse response = null;
        try {
            response = ClientApi.captureAPayment(payment_id, paymentsRequest);
            paymentPluginStatus = convertPaymentStatus(response.getStatus());
            try {
                hyperswitchRecord = this.hyperswitchDao.addResponse(
                    kbAccountId,
                    kbPaymentId,
                    kbTransactionId,
                    kbPaymentMethodId,
                    TransactionType.CAPTURE,
                    amount,
                    currency,
                    response,
                    DateTime.now(),
                    context.getTenantId());
            } catch (final SQLException e) {
                throw new PaymentPluginApiException("Unable to refresh payment", e);
            }
        } catch (com.hyperswitch.client.ApiException e) {
            throw new PaymentPluginApiException("Failed to capture payment: " + e.getMessage(), e);
        }

        return new HyperswitchPaymentTransactionInfoPlugin(
            hyperswitchRecord,
            kbPaymentId,
            kbTransactionId,
            TransactionType.CAPTURE,
            amount,
            currency,
            paymentPluginStatus,
            response.getErrorMessage(),
            response.getErrorCode(),
            response.getPaymentId(),
            response.getReferenceId(),
            DateTime.now(),
            DateTime.now(),
            null);
    }

    @Override
    public PaymentTransactionInfoPlugin purchasePayment(final UUID kbAccountId,
                                                        final UUID kbPaymentId,
                                                        final UUID kbTransactionId,
                                                        final UUID kbPaymentMethodId,
                                                        final BigDecimal amount,
                                                        final Currency currency,
                                                        final Iterable<PluginProperty> properties,
                                                        final CallContext context) throws PaymentPluginApiException {

        logger.info("[purchasePayment] calling purchase payment for account {}", kbAccountId);

        try {
            // Get mandate from our storage
            HyperswitchPaymentMethodsRecord paymentMethod = this.hyperswitchDao.getPaymentMethod(kbPaymentMethodId.toString());
            if (paymentMethod == null || paymentMethod.getHyperswitchId() == null) {
                throw new PaymentPluginApiException("No mandate found for payment method", new IllegalStateException());
            }

            // Build payment request
            PaymentsCreateRequest paymentsCreateRequest = new PaymentsCreateRequest();
            paymentsCreateRequest.setAmount(KillBillMoney.toMinorUnits(currency.toString(), amount));
            paymentsCreateRequest.setCurrency(convertCurrency(currency));
            paymentsCreateRequest.confirm(true);
            paymentsCreateRequest.profileId(hyperswitchConfigurationHandler.getConfigurable(context.getTenantId()).getProfileId());
            paymentsCreateRequest.customerId(kbAccountId.toString());
            paymentsCreateRequest.offSession(true);
/*            RecurringDetailsOneOf1 recurringDetailsImpl = new RecurringDetailsOneOf1()
                .type(RecurringDetailsOneOf1.TypeEnum.PAYMENT_METHOD_ID)
                .data(paymentMethod.getHyperswitchId());

            paymentsCreateRequest.setRecurringDetails(recurringDetailsImpl);*/

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.createObjectNode()
                .put("type", "payment_method_id")
                .put("data", paymentMethod.getHyperswitchId());
            RecurringDetails details = mapper.convertValue(node, RecurringDetails.class);
            paymentsCreateRequest.setRecurringDetails(details);


            // Make API call
            PaymentsApi clientApi = buildHyperswitchClient(context);
            PaymentsResponse response;
            try {
                response = clientApi.createAPayment(paymentsCreateRequest);
            } catch (com.hyperswitch.client.ApiException e) {
                throw new PaymentPluginApiException("Failed to process purchase payment: " + e.getMessage(), e);
            }

            // Process response
            PaymentPluginStatus paymentPluginStatus = convertPaymentStatus(response.getStatus());

            // Store in our responses table
            HyperswitchResponsesRecord hyperswitchRecord = this.hyperswitchDao.addResponse(
                kbAccountId,
                kbPaymentId,
                kbTransactionId,
                kbPaymentMethodId,
                TransactionType.PURCHASE,
                amount,
                currency,
                response,
                clock.getUTCNow(),
                context.getTenantId()
            );

            return new HyperswitchPaymentTransactionInfoPlugin(
                hyperswitchRecord,
                kbPaymentId,
                kbTransactionId,
                TransactionType.PURCHASE,
                amount,
                currency,
                paymentPluginStatus,
                response.getErrorMessage(),
                response.getErrorCode(),
                response.getPaymentId(),
                response.getReferenceId(),
                DateTime.now(),
                DateTime.now(),
                (List<PluginProperty>) properties
            );

        } catch (SQLException e) {
            throw new PaymentPluginApiException("Failed to process purchase payment", e);
        }
    }

    @Override
    public PaymentTransactionInfoPlugin voidPayment(final UUID kbAccountId, final UUID kbPaymentId,
                                                    final UUID kbTransactionId, final UUID kbPaymentMethodId, final Iterable<PluginProperty> properties,
                                                    final CallContext context) throws PaymentPluginApiException {
        HyperswitchResponsesRecord hyperswitchRecord = null;
        try {
            hyperswitchRecord = this.hyperswitchDao.getSuccessfulResponse(kbPaymentId, context.getTenantId());
        } catch (SQLException e) {
            logger.error("[voidPayment] but we encountered a database error", e);
            return HyperswitchPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
                TransactionType.VOID, "[voidPayment] but we encountered a database error");
        }

        String payment_id = hyperswitchRecord.getPaymentAttemptId();
        PaymentsCancelRequest paymentsRequest = new PaymentsCancelRequest();
        PaymentsApi ClientApi = buildHyperswitchClient(context);

        try {
            ClientApi.cancelAPayment(payment_id, paymentsRequest);

            PaymentPluginStatus paymentPluginStatus = PaymentPluginStatus.CANCELED;

            // Create a PaymentsResponse object for database recording
            PaymentsResponse paymentsResponse = new PaymentsResponse();
            paymentsResponse.setPaymentId(payment_id);
            paymentsResponse.setStatus(IntentStatus.CANCELLED);

            try {
                hyperswitchRecord = this.hyperswitchDao.addResponse(
                    kbAccountId,
                    kbPaymentId,
                    kbTransactionId,
                    kbPaymentMethodId,
                    TransactionType.VOID,
                    hyperswitchRecord.getAmount(),
                    null,
                    paymentsResponse,
                    DateTime.now(),
                    context.getTenantId());
            } catch (final SQLException e) {
                throw new PaymentPluginApiException("Unable to save void payment response", e);
            }

            return new HyperswitchPaymentTransactionInfoPlugin(
                hyperswitchRecord,
                kbPaymentId,
                kbTransactionId,
                TransactionType.VOID,
                hyperswitchRecord.getAmount(),
                null,
                paymentPluginStatus,
                paymentsResponse.getErrorMessage(),
                paymentsResponse.getErrorCode(),
                payment_id,
                null,
                DateTime.now(),
                DateTime.now(),
                null);

        } catch (com.hyperswitch.client.ApiException e) {
            throw new PaymentPluginApiException("Void payment failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentTransactionInfoPlugin creditPayment(final UUID kbAccountId, final UUID kbPaymentId,
                                                      final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency,
                                                      final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public PaymentTransactionInfoPlugin refundPayment(final UUID kbAccountId, final UUID kbPaymentId,
                                                      final UUID kbTransactionId, final UUID kbPaymentMethodId, final BigDecimal amount, final Currency currency,
                                                      final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        logger.info("Refund Payment for account {}", kbAccountId);
        HyperswitchResponsesRecord hyperswitchRecord = null;
        try {
            hyperswitchRecord = this.hyperswitchDao.getSuccessfulResponse(kbPaymentId, context.getTenantId());
            if (this.refundValidations(hyperswitchRecord, amount) != null) {
                return this.refundValidations(hyperswitchRecord, amount);
            }
        } catch (SQLException e) {
            logger.error("[refundPayment]  but we encountered a database error", e);
            return HyperswitchPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
                TransactionType.REFUND, "[refundPayment] but we encountered a database error");
        }
        PaymentPluginStatus paymentPluginStatus = null;
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setPaymentId(hyperswitchRecord.getPaymentAttemptId());
        Long refundAmount = KillBillMoney.toMinorUnits(hyperswitchRecord.getCurrency(), amount);
        refundRequest.setAmount(refundAmount);
        RefundsApi ClientApi = buildHyperswitchRefundsClient(context);
        RefundResponse response = null;
        try {
            response = ClientApi.createARefund(refundRequest);
            paymentPluginStatus = convertRefundStatus(response.getStatus());
            try {
                hyperswitchRecord = this.hyperswitchDao.addResponse(
                    kbAccountId,
                    kbPaymentId, kbTransactionId, TransactionType.REFUND, amount, currency, response,
                    DateTime.now(), context.getTenantId());
            } catch (final SQLException e) {
                throw new PaymentPluginApiException("Unable to refresh payment", e);
            }
        } catch (com.hyperswitch.client.ApiException e) {
            throw new PaymentPluginApiException("Failed to process refund: " + e.getMessage(), e);
        }

        return new HyperswitchPaymentTransactionInfoPlugin(
            hyperswitchRecord,
            kbPaymentId,
            kbTransactionId,
            TransactionType.REFUND,
            amount,
            currency,
            paymentPluginStatus,
            response.getErrorMessage(),
            response.getErrorCode(),
            response.getPaymentId(),
            null,
            DateTime.now(),
            DateTime.now(),
            null);
    }

    @Override
    public List<PaymentTransactionInfoPlugin> getPaymentInfo(final UUID kbAccountId, final UUID kbPaymentId,
                                                             final Iterable<PluginProperty> properties, final TenantContext context) throws PaymentPluginApiException {
        logger.info("[getPaymentInfo] getPaymentInfo for account {}", kbAccountId);
        final List<PaymentTransactionInfoPlugin> transactions = super.getPaymentInfo(kbAccountId, kbPaymentId,
            properties, context);

        if (transactions.isEmpty()) {
            // We don't know about this payment (maybe it was aborted in a control plugin)
            return transactions;
        }

        boolean wasRefreshed = false;
        for (final PaymentTransactionInfoPlugin transaction : transactions) {
            if (transaction.getStatus() == PaymentPluginStatus.PENDING) {
                HyperswitchResponsesRecord hyperswitchRecord = null;
                final String paymentIntentId = PluginProperties.findPluginPropertyValue("payment_id",
                    transaction.getProperties());

                PaymentsApi ClientApi = buildHyperswitchClient(context);
                try {
                    // Create retrieve request with force_sync flag
                    PaymentRetrieveBody retrieveBody = new PaymentRetrieveBody();
                    retrieveBody.setForceSync(true);

                    // Call the new retrieve API
                    PaymentsResponse response = ClientApi.retrieveAPayment(paymentIntentId, retrieveBody);

                    try {
                        hyperswitchRecord = this.hyperswitchDao.updateResponse(
                            kbAccountId,
                            response,
                            context.getTenantId());
                    } catch (final SQLException e) {
                        throw new PaymentPluginApiException("Unable to refresh payment", e);
                    }

                    wasRefreshed = true;
                } catch (com.hyperswitch.client.ApiException e) {
                    throw new PaymentPluginApiException("Failed to retrieve payment info: " + e.getMessage(), e);
                }
            }
        }

        return wasRefreshed ? super.getPaymentInfo(kbAccountId, kbPaymentId, properties, context) : transactions;
    }

    @Override
    public Pagination<PaymentTransactionInfoPlugin> searchPayments(final String searchKey, final Long offset,
                                                                   final Long limit, final Iterable<PluginProperty> properties, final TenantContext context)
        throws PaymentPluginApiException {
        return null;
    }

    @Override
    public void addPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId,
                                 final PaymentMethodPlugin paymentMethodProps, final boolean setDefault,
                                 final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        logger.info("[addPaymentMethod] Adding Payment Method");
        final Map<String, String> allProperties = PluginProperties.toStringMap(paymentMethodProps.getProperties(),
            properties);
        // Extract client_secret if present
        String clientSecret = allProperties.get("client_secret");

        try {
            this.hyperswitchDao.addPaymentMethod(
                kbAccountId,
                kbPaymentMethodId,
                allProperties,
                "",
                clientSecret,
                context.getTenantId());
        } catch (SQLException e) {
            throw new PaymentPluginApiException("Error calling Hyperswitch while adding payment method", e);
        }
    }

    @Override
    public void deletePaymentMethod(final UUID kbAccountId,
                                    final UUID kbPaymentMethodId,
                                    final Iterable<PluginProperty> properties,
                                    final CallContext context) throws PaymentPluginApiException {

        try {
            // Get payment method to check if there's a mandate to delete
            HyperswitchPaymentMethodsRecord paymentMethod = hyperswitchDao.getPaymentMethod(kbPaymentMethodId.toString());

            if (paymentMethod != null && paymentMethod.getHyperswitchId() != null) {
                // Delete mandate in Hyperswitch if it exists
                PaymentMethodsApi clientApi = buildHyperswitchPaymentMethodsClient(context);
                try {
                    clientApi.deleteAPaymentMethod(paymentMethod.getHyperswitchId());
                } catch (com.hyperswitch.client.ApiException e) {
                    logger.warn("Failed to delete mandate in Hyperswitch: {}", e.getMessage());
                }
            }

            // Delete our record
            hyperswitchDao.deletePaymentMethod(kbPaymentMethodId, context.getTenantId());

        } catch (SQLException e) {
            throw new PaymentPluginApiException("Failed to delete payment method", e);
        }
    }

    @Override
    public PaymentMethodPlugin getPaymentMethodDetail(final UUID kbAccountId,
                                                      final UUID kbPaymentMethodId,
                                                      final Iterable<PluginProperty> properties,
                                                      final TenantContext context) throws PaymentPluginApiException {
        try {
            final HyperswitchPaymentMethodsRecord record = this.hyperswitchDao.getPaymentMethod(kbPaymentMethodId.toString());
            if (record == null) {
                return null;
            }
            return buildPaymentMethodPlugin(record);
        } catch (SQLException e) {
            throw new PaymentPluginApiException("Failed to retrieve payment method", e);
        }
    }

    @Override
    public void setDefaultPaymentMethod(final UUID kbAccountId, final UUID kbPaymentMethodId,
                                        final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {

    }

    @Override
    public List<PaymentMethodInfoPlugin> getPaymentMethods(final UUID kbAccountId, final boolean refreshFromGateway,
                                                           final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {
        return null;
    }

    @Override
    public Pagination<PaymentMethodPlugin> searchPaymentMethods(final String searchKey, final Long offset,
                                                                final Long limit, final Iterable<PluginProperty> properties, final TenantContext context)
        throws PaymentPluginApiException {
        return null;
    }

    @Override
    public void resetPaymentMethods(final UUID kbAccountId, final List<PaymentMethodInfoPlugin> paymentMethods,
                                    final Iterable<PluginProperty> properties, final CallContext context) throws PaymentPluginApiException {

    }

    @Override
    public HostedPaymentPageFormDescriptor buildFormDescriptor(final UUID kbAccountId,
                                                               final Iterable<PluginProperty> customFields, final Iterable<PluginProperty> properties,
                                                               final CallContext context) throws PaymentPluginApiException {
        return null;
    }

    private PaymentPluginStatus convertPaymentStatus(IntentStatus paymentStatus) {
        switch (paymentStatus) {
            case SUCCEEDED:
            case PARTIALLY_CAPTURED:
            case PARTIALLY_CAPTURED_AND_CAPTURABLE:
                return PaymentPluginStatus.PROCESSED;
            case PROCESSING:
            case REQUIRES_PAYMENT_METHOD:
            case REQUIRES_CONFIRMATION:
            case REQUIRES_CUSTOMER_ACTION:
            case REQUIRES_MERCHANT_ACTION:
            case REQUIRES_CAPTURE:
                return PaymentPluginStatus.PENDING;
            case CANCELLED:
                return PaymentPluginStatus.CANCELED;
            case FAILED:
                return PaymentPluginStatus.ERROR;
            default:
                return PaymentPluginStatus.UNDEFINED;
        }
    }

    private PaymentPluginStatus convertRefundStatus(RefundStatus refundStatus) {
        switch (refundStatus) {
            case SUCCEEDED:
                return PaymentPluginStatus.PROCESSED;
            case PENDING:
                return PaymentPluginStatus.PENDING;
            case FAILED:
                return PaymentPluginStatus.ERROR;
            case REVIEW:
                return PaymentPluginStatus.PENDING;
            default:
                return PaymentPluginStatus.UNDEFINED;
        }
    }

    private com.hyperswitch.client.model.Currency convertCurrency(
        Currency currency) {
        switch (currency) {
            case USD:
                return com.hyperswitch.client.model.Currency.USD;
            case AUD:
                return com.hyperswitch.client.model.Currency.AUD;
            case CAD:
                return com.hyperswitch.client.model.Currency.CAD;
            case DKK:
                return com.hyperswitch.client.model.Currency.DKK;
            case EUR:
                return com.hyperswitch.client.model.Currency.EUR;
            case GBP:
                return com.hyperswitch.client.model.Currency.GBP;
            case NZD:
                return com.hyperswitch.client.model.Currency.NZD;
            case SEK:
                return com.hyperswitch.client.model.Currency.SEK;
            default:
                return null;

        }
    }

    @Override
    protected PaymentTransactionInfoPlugin buildPaymentTransactionInfoPlugin(
        final HyperswitchResponsesRecord hyperswitchRecord) {
        try {
            // Get the payment method record using the payment ID
            HyperswitchPaymentMethodsRecord paymentMethodRecord =
                this.hyperswitchDao.getPaymentMethodByPaymentId(hyperswitchRecord.getKbPaymentId());

            return HyperswitchPaymentTransactionInfoPlugin.build(hyperswitchRecord, paymentMethodRecord);
        } catch (SQLException e) {
            logger.warn("Failed to retrieve payment method for client secret", e);
            return HyperswitchPaymentTransactionInfoPlugin.build(hyperswitchRecord, null);
        }
    }

    @Override
    protected PaymentMethodPlugin buildPaymentMethodPlugin(HyperswitchPaymentMethodsRecord record) {
        List<PluginProperty> properties = new ArrayList<>();
        if (record.getHyperswitchId() != null) {
            properties.add(new PluginProperty("payment_id", record.getHyperswitchId(), false));
        }

        return new PluginPaymentMethodPlugin(
            UUID.fromString(record.getKbPaymentMethodId()),
            record.getHyperswitchId(),
            record.getIsDefault() == 1,
            properties
        );
    }

    @Override
    protected PaymentMethodInfoPlugin buildPaymentMethodInfoPlugin(HyperswitchPaymentMethodsRecord record) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buildPaymentMethodInfoPlugin'");
    }

    @Override
    protected String getPaymentMethodId(HyperswitchPaymentMethodsRecord input) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPaymentMethodId'");
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> T buildHyperswitchClient(final TenantContext tenantContext, final Class<T> apiClass) {
        UUID tenantId = tenantContext.getTenantId();

        // Check cache first
        Map<Class<?>, Object> tenantClients = clientCache.get(tenantId);
        if (tenantClients != null) {
            T cachedClient = (T) tenantClients.get(apiClass);
            if (cachedClient != null) {
                return cachedClient;
            }
        }

        // Get config
        final HyperswitchConfigProperties config = hyperswitchConfigurationHandler.getConfigurable(tenantId);
        if (config == null || config.getHSApiKey() == null || config.getHSApiKey().isEmpty()) {
            logger.warn("Per-tenant properties not configured");
            return null;
        }

        // Create new client
        try {
            com.hyperswitch.client.ApiClient apiClient = new com.hyperswitch.client.ApiClient();
            ((ApiKeyAuth) apiClient.getAuthentication("api_key")).setApiKey(config.getHSApiKey());
            T newClient = apiClass.getConstructor(com.hyperswitch.client.ApiClient.class).newInstance(apiClient);

            // Cache the new client
            if (tenantClients == null) {
                tenantClients = new HashMap<>();
                clientCache.put(tenantId, tenantClients);
            }
            tenantClients.put(apiClass, newClient);

            return newClient;
        } catch (Exception e) {
            logger.error("Failed to create API client", e);
            return null;
        }
    }

    private synchronized void clearClientCache(UUID tenantId) {
        clientCache.remove(tenantId);
    }

    private synchronized void clearAllClientCaches() {
        clientCache.clear();
    }

    private PaymentsApi buildHyperswitchClient(final TenantContext tenantContext) {
        return buildHyperswitchClient(tenantContext, PaymentsApi.class);
    }

    private PaymentMethodsApi buildHyperswitchPaymentMethodsClient(final TenantContext tenantContext) {
        return buildHyperswitchClient(tenantContext, PaymentMethodsApi.class);
    }

    private RefundsApi buildHyperswitchRefundsClient(final TenantContext tenantContext) {
        return buildHyperswitchClient(tenantContext, RefundsApi.class);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String verifyWebhookSignature(String payload, String signature, String secretKey) throws PaymentPluginApiException {
        try {
            // Generate HMAC-SHA512 signature using the raw payload string
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Hex.encodeHexString(hmac);

            // Compare signatures using constant-time comparison
            if (!MessageDigest.isEqual(signature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                throw new PaymentPluginApiException("Signature verification failed", new IllegalStateException());
            }

            return payload;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new PaymentPluginApiException("Error verifying webhook signature", e);
        }
    }

    public GatewayNotification processNotification(final String notification,
                                                   final Iterable<PluginProperty> properties,
                                                   final CallContext context) throws PaymentPluginApiException {
        try {
            // Get webhook signature from properties
            String signature = null;
            for (PluginProperty property : properties) {
                if ("x-webhook-signature-512".equals(property.getKey())) {
                    signature = (String) property.getValue();
                    break;
                }
            }

            if (signature == null || signature.isEmpty()) {
                throw new PaymentPluginApiException("Missing webhook signature", new IllegalStateException());
            }

            // Get API key for signature verification
            final HyperswitchConfigProperties config = hyperswitchConfigurationHandler.getConfigurable(context.getTenantId());
            if (config == null || config.getWebhookSecret() == null) {
                throw new PaymentPluginApiException("Missing webhook signing key configuration", new IllegalStateException());
            }

            // Verify webhook signature
            String verifiedPayload = verifyWebhookSignature(notification, signature, config.getWebhookSecret());

            // Process the verified webhook payload
            final JsonNode event = objectMapper.readTree(verifiedPayload);
            final String eventId = event.path("event_id").asText();
            final String eventType = event.path("event_type").asText();
            final JsonNode content = event.path("content").path("object");
            final String paymentId = content.path("payment_id").asText();
            final String status = content.path("status").asText();
            final String errorCode = content.path("error_code").asText();
            final String errorMessage = content.path("error_message").asText();

            // Check for duplicate event
            if (hyperswitchDao.isEventProcessed(eventId, context.getTenantId())) {
                logger.info("Skipping duplicate webhook event {}", eventId);
                return null;
            }

            // Find the payment record
            final HyperswitchResponsesRecord response = this.hyperswitchDao.getResponseByPaymentAttemptId(
                paymentId, context.getTenantId());

            if (response == null) {
                logger.warn("Unable to find payment record for webhook notification: {}", paymentId);
                return null;
            }

            // Store webhook event
            this.hyperswitchDao.addWebhookEvent(
                UUID.fromString(response.getKbAccountId()),
                UUID.fromString(response.getKbPaymentId()),
                UUID.fromString(response.getKbPaymentTransactionId()),
                eventId,
                eventType,
                paymentId,
                status,
                errorCode,
                errorMessage,
                notification,
                context.getTenantId());

            // Handle payment updates based on status
            handlePaymentUpdate(eventType, status, content, response, context);

            // Update payment status
            updatePaymentStatus(response, status, errorCode, errorMessage);

            return new HyperswitchGatewayNotification(UUID.fromString(response.getKbPaymentId()));

        } catch (Exception e) {
            throw new PaymentPluginApiException("Error processing notification: " + e.getMessage(), e);
        }
    }

    private void handlePaymentUpdate(String eventType, String status, JsonNode content,
                                     HyperswitchResponsesRecord response, CallContext context)
        throws AccountApiException, PaymentApiException {

        // Handle payment method updates
        String paymentMethodId = content.path("payment_method_id").asText();
        if (paymentMethodId != null && !paymentMethodId.isEmpty()) {
            try {
                this.hyperswitchDao.updateHyperswitchId(
                    UUID.fromString(response.getKbPaymentId()),
                    paymentMethodId,
                    UUID.fromString(response.getKbTenantId()));
            } catch (SQLException e) {
                logger.warn("Failed to update payment method ID: {}", paymentMethodId, e);
            }
        }

        // Handle authorization success for off-session setup payments
        if (eventType.equals("payment_succeeded") && "succeeded".equals(status)) {
            try {
                // Check if this was a setup payment (amount = 0)
                // Get amount from webhook content
                BigDecimal webhookAmount = new BigDecimal(content.path("amount").asText("0"));

                if (webhookAmount.compareTo(BigDecimal.ZERO) == 0) {
                    // Get account and payment info
                    Account account = killbillAPI.getAccountUserApi().getAccountById(
                        UUID.fromString(response.getKbAccountId()),
                        context
                    );

                    Payment setupPayment = killbillAPI.getPaymentApi().getPayment(
                        UUID.fromString(response.getKbPaymentId()),
                        false,
                        false,
                        Collections.emptyList(),
                        context
                    );

                    // Get the actual amount from the original authorization
                    BigDecimal amount = setupPayment.getAuthAmount();
                    if (amount.compareTo(BigDecimal.ZERO) == 0) {
                        amount = setupPayment.getTransactions()
                            .stream()
                            .map(PaymentTransaction::getAmount)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    }

                    // Create a new Kill Bill payment transaction using the wrapper
                    PaymentApiWrapper wrapper = getPaymentApiWrapper();
                    wrapper.createPayment(
                        TransactionType.PURCHASE,
                        account,
                        UUID.fromString(response.getKbPaymentMethodId()),
                        null, // New payment, so no existing payment ID
                        amount,
                        setupPayment.getCurrency(),
                        null, // New payment external key
                        null, // New transaction external key
                        Collections.emptyList(),
                        context
                    );

                    // Notify Kill Bill the setup payment is complete
                    getPaymentApiWrapper().transitionPendingTransaction(
                        account,
                        setupPayment.getId(),
                        UUID.fromString(response.getKbPaymentTransactionId()),
                        PaymentPluginStatus.PROCESSED,
                        context
                    );
                } else {
                    // Handle normal payment success
                    Account account = killbillAPI.getAccountUserApi().getAccountById(
                        UUID.fromString(response.getKbAccountId()),
                        context
                    );

                    getPaymentApiWrapper().transitionPendingTransaction(
                        account,
                        UUID.fromString(response.getKbPaymentId()),
                        UUID.fromString(response.getKbPaymentTransactionId()),
                        PaymentPluginStatus.PROCESSED,
                        context
                    );
                }
            } catch (Exception e) {
                logger.error("Failed to process payment webhook", e);
            }
        }
    }

    private void updatePaymentStatus(HyperswitchResponsesRecord response, String status,
                                     String errorCode, String errorMessage) throws SQLException {
        final Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("status", status);
        if (errorCode != null && !errorCode.isEmpty() && !errorCode.equals("null")) {
            additionalData.put("error_code", errorCode);
        }
        if (errorMessage != null && !errorMessage.isEmpty() && !errorCode.equals("null")) {
            additionalData.put("error_message", errorMessage);
        }

        this.hyperswitchDao.updateResponse(response, additionalData);
    }

    public PaymentTransactionInfoPlugin refundValidations(
        HyperswitchResponsesRecord hyperswitchRecord, BigDecimal amount) {
        if (hyperswitchRecord == null) {
            logger.error("[refundPayment] Purchase does not exist");
            return HyperswitchPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
                TransactionType.REFUND, "Purchase does not exist");
        }

        // Skip validation if amount is zero
        if (amount != null && BigDecimal.ZERO.compareTo(amount) == 0) {
            return null;
        }

        // Only validate non-zero amounts
        if (amount != null && hyperswitchRecord.getAmount().compareTo(amount) < 0) {
            logger.error("[refundPayment] The refund amount is more than the transaction amount");
            return HyperswitchPaymentTransactionInfoPlugin.cancelPaymentTransactionInfoPlugin(
                TransactionType.REFUND, "The refund amount is more than the transaction amount");
        }

        return null;
    }

    private PaymentApiWrapper getPaymentApiWrapper() {
        return new PaymentApiWrapper(killbillAPI, true);
    }

}
