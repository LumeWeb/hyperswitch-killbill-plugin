/*
 * Copyright 2021 The Billing Project, LLC
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;

import org.joda.time.DateTime;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.payment.plugin.api.PaymentPluginStatus;
import org.killbill.billing.payment.plugin.api.PaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.api.payment.PluginPaymentTransactionInfoPlugin;
import org.killbill.billing.plugin.hyperswitch.dao.HyperswitchDao;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records.HyperswitchPaymentMethodsRecord;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records.HyperswitchResponsesRecord;

import com.google.common.base.Strings;

public class HyperswitchPaymentTransactionInfoPlugin extends PluginPaymentTransactionInfoPlugin {

	public static HyperswitchPaymentTransactionInfoPlugin build(
			final HyperswitchResponsesRecord hyperswitchResponsesRecord,
			final HyperswitchPaymentMethodsRecord paymentMethodRecord) {
		final Map<?, ?> additionalData = HyperswitchDao
				.mapFromAdditionalDataString(hyperswitchResponsesRecord.getAdditionalData());

        List<PluginProperty> properties = new ArrayList<>();
        if (paymentMethodRecord != null && paymentMethodRecord.getClientSecret() != null) {
            properties.add(new PluginProperty("client_secret", paymentMethodRecord.getClientSecret(), false));
        } else {
            // Fallback to additional data if payment method not found
            final String clientSecret = (String) additionalData.get("client_secret");
            if (clientSecret != null) {
                properties.add(new PluginProperty("client_secret", clientSecret, false));
            }
        }

		final String firstPaymentReferenceId = hyperswitchResponsesRecord.getPaymentAttemptId();

		final DateTime responseDate = DateTime.now();
		return new HyperswitchPaymentTransactionInfoPlugin(
				hyperswitchResponsesRecord,
				UUID.fromString(hyperswitchResponsesRecord.getKbPaymentId()),
				UUID.fromString(hyperswitchResponsesRecord.getKbPaymentTransactionId()),
				TransactionType.valueOf(hyperswitchResponsesRecord.getTransactionType()),
				hyperswitchResponsesRecord.getAmount(),
				Strings.isNullOrEmpty(hyperswitchResponsesRecord.getCurrency())
						? null
						: Currency.valueOf(HyperswitchResponsesRecord.getCurrency()),
				getPaymentPluginStatus(additionalData),
				HyperswitchResponsesRecord.getErrorMessage(),
				HyperswitchResponsesRecord.getErrorCode(),
				firstPaymentReferenceId,
				null,
				responseDate,
				responseDate,
				PluginProperties.buildPluginProperties(additionalData));
	}

	public HyperswitchPaymentTransactionInfoPlugin(final HyperswitchResponsesRecord hyperswitchResponsesRecord,
			UUID kbPaymentId, UUID kbTransactionPaymentPaymentId,
			TransactionType transactionType, BigDecimal amount, Currency currency, PaymentPluginStatus pluginStatus,
			String gatewayError, String gatewayErrorCode, String firstPaymentReferenceId,
			String secondPaymentReferenceId, DateTime createdDate, DateTime effectiveDate,
			List<PluginProperty> properties) {
		super(kbPaymentId, kbTransactionPaymentPaymentId, transactionType, amount, currency, pluginStatus, gatewayError,
				gatewayErrorCode, firstPaymentReferenceId, secondPaymentReferenceId, createdDate, effectiveDate,
				properties);
	}

	public static PaymentTransactionInfoPlugin cancelPaymentTransactionInfoPlugin(
			TransactionType transactionType, String message) {

		return new HyperswitchPaymentTransactionInfoPlugin(
				null,
				null,
				null,
				transactionType,
				null,
				null,
				PaymentPluginStatus.CANCELED,
				message,
				null,
				null,
				null,
				null,
				null,
				null);
	}

	private static PaymentPluginStatus getPaymentPluginStatus(final Map additionalData) {
        final String status = (String) additionalData.get("status");
        if ("succeeded".equals(status) || "requires_capture".equals(status)) {
            return PaymentPluginStatus.PROCESSED;
        } else if ("processing".equals(status) ||
                   "requires_payment_method".equals(status) ||
                   "requires_confirmation".equals(status) ||
                   "requires_customer_action".equals(status)) {
            return PaymentPluginStatus.PENDING;
        } else if ("failed".equals(status)) {
            return PaymentPluginStatus.ERROR;
        } else if ("cancelled".equals(status)) {
            return PaymentPluginStatus.CANCELED;
        } else {
            return PaymentPluginStatus.UNDEFINED;
        }
    }

}
