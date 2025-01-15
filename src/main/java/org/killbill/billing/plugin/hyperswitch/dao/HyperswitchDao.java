/*
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

package org.killbill.billing.plugin.hyperswitch.dao;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.api.TransactionType;
import org.killbill.billing.plugin.api.PluginProperties;
import org.killbill.billing.plugin.dao.payment.PluginPaymentDao;
import org.killbill.billing.plugin.hyperswitch.HyperswitchPluginProperties;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchPaymentMethods;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchResponses;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records.HyperswitchPaymentMethodsRecord;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records.HyperswitchResponsesRecord;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records.HyperswitchWebhookEventsRecord;
import org.killbill.billing.plugin.hyperswitch.exception.FormaterException;
import org.killbill.clock.Clock;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.hyperswitch.client.model.PaymentsResponse;
import com.hyperswitch.client.model.RefundResponse;

import static org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS;
import static org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchResponses.HYPERSWITCH_RESPONSES;
import static org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchWebhookEvents.HYPERSWITCH_WEBHOOK_EVENTS;

public class HyperswitchDao extends
    PluginPaymentDao<HyperswitchResponsesRecord, HyperswitchResponses, HyperswitchPaymentMethodsRecord, HyperswitchPaymentMethods> {

    private static final ObjectMapper staticObjectMapper = new ObjectMapper();

    static {
        staticObjectMapper.setSerializationInclusion(Include.NON_EMPTY);
    }

    private final Clock clock;

    public HyperswitchDao(final DataSource dataSource, final Clock clock) throws SQLException {
        super(HYPERSWITCH_RESPONSES, HYPERSWITCH_PAYMENT_METHODS, dataSource);
        // Save space in the database
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        this.clock = clock;
    }

    // Payment methods

    public void addPaymentMethod(final UUID kbAccountId,
                                 final UUID kbPaymentMethodId,
                                 final Map<String, String> additionalDataMap,
                                 final String hyperswitchId,
                                 final String clientSecret,
                                 final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
            new WithConnectionCallback<HyperswitchResponsesRecord>() {
                @Override
                public HyperswitchResponsesRecord withConnection(final Connection conn) throws SQLException {
                    System.out.println("Adding payment method");
                    DSL.using(conn, dialect, settings)
                        .insertInto(HYPERSWITCH_PAYMENT_METHODS,
                            HYPERSWITCH_PAYMENT_METHODS.KB_ACCOUNT_ID,
                            HYPERSWITCH_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID,
                            HYPERSWITCH_PAYMENT_METHODS.HYPERSWITCH_ID,
                            HYPERSWITCH_PAYMENT_METHODS.IS_DEFAULT,
                            HYPERSWITCH_PAYMENT_METHODS.CLIENT_SECRET,
                            HYPERSWITCH_PAYMENT_METHODS.IS_DELETED,
                            HYPERSWITCH_PAYMENT_METHODS.ADDITIONAL_DATA,
                            HYPERSWITCH_PAYMENT_METHODS.CREATED_DATE,
                            HYPERSWITCH_PAYMENT_METHODS.UPDATED_DATE,
                            HYPERSWITCH_PAYMENT_METHODS.KB_TENANT_ID)
                        .values(kbAccountId.toString(),
                            kbPaymentMethodId.toString(),
                            hyperswitchId,
                            (short) FALSE,  // is_default
                            clientSecret,
                            (short) FALSE,  // is_deleted
                            asString(additionalDataMap),
                            toLocalDateTime(new DateTime()),
                            toLocalDateTime(new DateTime()),
                            kbTenantId.toString())
                        .execute();
                    System.out.println("Added payment method successfully");
                    return null;
                }
            });
    }

    public void updateHyperswitchId(final UUID kbPaymentMethodId,
                                    final String mandateId,
                                    final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings)
                .update(HYPERSWITCH_PAYMENT_METHODS)
                .set(HYPERSWITCH_PAYMENT_METHODS.HYPERSWITCH_ID, mandateId)
                .where(HYPERSWITCH_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID.equal(kbPaymentMethodId.toString()))
                .and(HYPERSWITCH_PAYMENT_METHODS.KB_TENANT_ID.equal(kbTenantId.toString()))
                .execute());
    }

    public HyperswitchPaymentMethodsRecord getPaymentMethod(final String kbPaymentMethodId)
        throws SQLException {
        return execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings)
                .selectFrom(HYPERSWITCH_PAYMENT_METHODS)
                .where(HYPERSWITCH_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID.equal(kbPaymentMethodId))
                .fetchOne());
    }

    public HyperswitchPaymentMethodsRecord getPaymentMethodByPaymentId(final String kbPaymentId)
        throws SQLException {
        // Get the initial/first response record for this payment
        final HyperswitchResponsesRecord response = execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings)
                .selectFrom(HYPERSWITCH_RESPONSES)
                .where(HYPERSWITCH_RESPONSES.KB_PAYMENT_ID.equal(kbPaymentId))
                .orderBy(HYPERSWITCH_RESPONSES.CREATED_DATE.asc())
                .limit(1)
                .fetchOne());

        if (response == null) {
            return null;
        }

        // Then get the payment method record
        return execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings)
                .selectFrom(HYPERSWITCH_PAYMENT_METHODS)
                .where(HYPERSWITCH_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID.equal(response.getKbPaymentMethodId()))
                .fetchOne());
    }

    public void deletePaymentMethod(final UUID kbPaymentMethodId,
                                    final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings)
                .deleteFrom(HYPERSWITCH_PAYMENT_METHODS)
                .where(HYPERSWITCH_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID.equal(kbPaymentMethodId.toString()))
                .and(HYPERSWITCH_PAYMENT_METHODS.KB_TENANT_ID.equal(kbTenantId.toString()))
                .execute());
    }

    public HyperswitchResponsesRecord addResponse(final UUID kbAccountId,
                                                  final UUID kbPaymentId,
                                                  final UUID kbPaymentTransactionId,
                                                  final UUID kbPaymentMethodId,
                                                  final TransactionType transactionType,
                                                  final BigDecimal amount,
                                                  final Currency currency,
                                                  final PaymentsResponse paymentsResponse,
                                                  final DateTime utcNow,
                                                  final UUID kbTenantId) throws SQLException {
        final Map<String, Object> additionalDataMap;
        additionalDataMap = HyperswitchPluginProperties.toAdditionalDataMap(paymentsResponse);
        return execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings).transactionResult(configuration -> {
                final DSLContext dslContext = DSL.using(configuration);
                dslContext.insertInto(HYPERSWITCH_RESPONSES,
                        HYPERSWITCH_RESPONSES.KB_ACCOUNT_ID,
                        HYPERSWITCH_RESPONSES.KB_PAYMENT_ID,
                        HYPERSWITCH_RESPONSES.KB_PAYMENT_TRANSACTION_ID,
                        HYPERSWITCH_RESPONSES.KB_PAYMENT_METHOD_ID,
                        HYPERSWITCH_RESPONSES.TRANSACTION_TYPE,
                        HYPERSWITCH_RESPONSES.AMOUNT,
                        HYPERSWITCH_RESPONSES.CURRENCY,
                        HYPERSWITCH_RESPONSES.PAYMENT_ATTEMPT_ID,
                        HYPERSWITCH_RESPONSES.ERROR_MESSAGE,
                        HYPERSWITCH_RESPONSES.ERROR_CODE,
                        HYPERSWITCH_RESPONSES.ADDITIONAL_DATA,
                        HYPERSWITCH_RESPONSES.CREATED_DATE,
                        HYPERSWITCH_RESPONSES.KB_TENANT_ID)
                    .values(kbAccountId.toString(),
                        kbPaymentId.toString(),
                        kbPaymentTransactionId.toString(),
                        kbPaymentMethodId.toString(),
                        transactionType.toString(),
                        amount,
                        currency == null ? null : currency.name(),
                        paymentsResponse.getPaymentId(),
                        paymentsResponse.getErrorMessage(),
                        paymentsResponse.getErrorCode(),
                        asString(additionalDataMap),
                        toLocalDateTime(utcNow),
                        kbTenantId.toString())
                    .execute();
                return dslContext.fetchOne(
                    HYPERSWITCH_RESPONSES,
                    HYPERSWITCH_RESPONSES.RECORD_ID
                        .eq(HYPERSWITCH_RESPONSES.RECORD_ID.getDataType().convert(dslContext.lastID())));
            }));
    }

    public HyperswitchResponsesRecord addResponse(final UUID kbAccountId,
                                                  final UUID kbPaymentId,
                                                  final UUID kbPaymentTransactionId,
                                                  final TransactionType transactionType,
                                                  final BigDecimal amount,
                                                  final Currency currency,
                                                  final RefundResponse refundResponse,
                                                  final DateTime utcNow,
                                                  final UUID kbTenantId) throws SQLException {
        final Map<String, Object> additionalDataMap;
        additionalDataMap = HyperswitchPluginProperties.toAdditionalDataMap(refundResponse);

        // First get the payment method ID from the original payment record
        HyperswitchPaymentMethodsRecord paymentMethod = getPaymentMethodByPaymentId(kbPaymentId.toString());
        if (paymentMethod == null) {
            throw new SQLException("Could not find payment method for payment ID: " + kbPaymentId);
        }

        return execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings).transactionResult(configuration -> {
                final DSLContext dslContext = DSL.using(configuration);
                dslContext.insertInto(HYPERSWITCH_RESPONSES,
                        HYPERSWITCH_RESPONSES.KB_ACCOUNT_ID,
                        HYPERSWITCH_RESPONSES.KB_PAYMENT_ID,
                        HYPERSWITCH_RESPONSES.KB_PAYMENT_TRANSACTION_ID,
                        HYPERSWITCH_RESPONSES.KB_PAYMENT_METHOD_ID,
                        HYPERSWITCH_RESPONSES.TRANSACTION_TYPE,
                        HYPERSWITCH_RESPONSES.AMOUNT,
                        HYPERSWITCH_RESPONSES.CURRENCY,
                        HYPERSWITCH_RESPONSES.PAYMENT_ATTEMPT_ID,
                        HYPERSWITCH_RESPONSES.ERROR_MESSAGE,
                        HYPERSWITCH_RESPONSES.ERROR_CODE,
                        HYPERSWITCH_RESPONSES.ADDITIONAL_DATA,
                        HYPERSWITCH_RESPONSES.CREATED_DATE,
                        HYPERSWITCH_RESPONSES.KB_TENANT_ID)
                    .values(kbAccountId.toString(),
                        kbPaymentId.toString(),
                        kbPaymentTransactionId.toString(),
                        paymentMethod.getKbPaymentMethodId(),
                        transactionType.toString(),
                        amount,
                        currency == null ? null : currency.name(),
                        refundResponse.getPaymentId(),
                        refundResponse.getErrorMessage(),
                        refundResponse.getErrorCode(),
                        asString(additionalDataMap),
                        toLocalDateTime(utcNow),
                        kbTenantId.toString())
                    .execute();
                return dslContext.fetchOne(
                    HYPERSWITCH_RESPONSES,
                    HYPERSWITCH_RESPONSES.RECORD_ID
                        .eq(HYPERSWITCH_RESPONSES.RECORD_ID.getDataType().convert(dslContext.lastID())));
            }));
    }

    public HyperswitchResponsesRecord updateResponse(final UUID kbPaymentTransactionId,
                                                     final PaymentsResponse hyperswitchPaymentResponse,
                                                     final UUID kbTenantId) throws SQLException {
        final Map<String, Object> additionalDataMap = HyperswitchPluginProperties.toAdditionalDataMap(
            hyperswitchPaymentResponse);
        return updateResponse(kbPaymentTransactionId, additionalDataMap, kbTenantId);
    }

    public HyperswitchResponsesRecord updateResponse(final UUID kbPaymentTransactionId,
                                                     final Iterable<PluginProperty> additionalPluginProperties,
                                                     final UUID kbTenantId) throws SQLException {
        final Map<String, Object> additionalProperties = PluginProperties.toMap(additionalPluginProperties);
        return updateResponse(kbPaymentTransactionId, additionalProperties,
            kbTenantId);
    }

    public HyperswitchResponsesRecord updateResponse(final UUID kbPaymentTransactionId,
                                                     final Map<String, Object> additionalProperties,
                                                     final UUID kbTenantId) throws SQLException {
        return execute(dataSource.getConnection(),
            new WithConnectionCallback<HyperswitchResponsesRecord>() {
                @Override
                public HyperswitchResponsesRecord withConnection(final Connection conn)
                    throws SQLException {
                    final HyperswitchResponsesRecord response = DSL.using(conn, dialect,
                            settings)
                        .selectFrom(HYPERSWITCH_RESPONSES)
                        .where(HYPERSWITCH_RESPONSES.KB_PAYMENT_TRANSACTION_ID
                            .equal(kbPaymentTransactionId.toString()))
                        .and(HYPERSWITCH_RESPONSES.KB_TENANT_ID.equal(kbTenantId.toString()))
                        .orderBy(HYPERSWITCH_RESPONSES.RECORD_ID.desc())
                        .limit(1)
                        .fetchOne();

                    if (response == null) {
                        return null;
                    }
                    final Map originalData = new HashMap(fromAdditionalData(response.getAdditionalData()));
                    originalData.putAll(additionalProperties);

                    DSL.using(conn, dialect, settings)
                        .update(HYPERSWITCH_RESPONSES)
                        .set(HYPERSWITCH_RESPONSES.ADDITIONAL_DATA, asString(originalData))
                        .where(HYPERSWITCH_RESPONSES.RECORD_ID.equal(response.getRecordId()))
                        .execute();
                    return response;
                }
            });
    }

    public void updateResponse(final HyperswitchResponsesRecord hyperswitchResponsesRecord,
                               final Map additionalMetadata) throws SQLException {
        final Map additionalDataMap = fromAdditionalData(hyperswitchResponsesRecord.getAdditionalData());
        additionalDataMap.putAll(additionalMetadata);

        execute(dataSource.getConnection(),
            new WithConnectionCallback<Void>() {
                @Override
                public Void withConnection(final Connection conn) throws SQLException {
                    DSL.using(conn, dialect, settings)
                        .update(HYPERSWITCH_RESPONSES)
                        .set(HYPERSWITCH_RESPONSES.ADDITIONAL_DATA, asString(additionalDataMap))
                        .where(HYPERSWITCH_RESPONSES.RECORD_ID.equal(hyperswitchResponsesRecord.getRecordId()))
                        .execute();
                    return null;
                }
            });
    }

    public HyperswitchResponsesRecord getSuccessfulResponse(
        final UUID kbPaymentId, final UUID kbTenantId) throws SQLException {
        return execute(
            dataSource.getConnection(),
            new WithConnectionCallback<HyperswitchResponsesRecord>() {
                @Override
                public HyperswitchResponsesRecord withConnection(final Connection conn) throws SQLException {
                    return DSL.using(conn, dialect, settings)
                        .selectFrom(HYPERSWITCH_RESPONSES)
                        .where(DSL.field(HYPERSWITCH_RESPONSES.KB_PAYMENT_ID).equal(kbPaymentId.toString()))
                        .and(DSL.field(HYPERSWITCH_RESPONSES.KB_TENANT_ID).equal(kbTenantId.toString()))
                        //                .and(DSL.field(HYPERSWITCH_RESPONSES.TRANSACTION_TYPE).equal(PURCHASE))
                        .orderBy(HYPERSWITCH_RESPONSES.RECORD_ID)
                        .fetchOne();
                }
            });
    }

    private Map fromAdditionalData(@Nullable final String additionalData) {
        if (additionalData == null) {
            return Collections.emptyMap();
        }

        try {
            return staticObjectMapper.readValue(additionalData, Map.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static Map mapFromAdditionalDataString(@Nullable final String additionalData) {
        if (additionalData == null) {
            return ImmutableMap.of();
        }

        try {
            return objectMapper.readValue(additionalData, Map.class);
        } catch (final IOException e) {
            throw new FormaterException(e);
        }
    }

    public void addWebhookEvent(
        final UUID kbAccountId,
        final UUID kbPaymentId,
        final UUID kbPaymentTransactionId,
        final String eventId,
        final String eventType,
        final String paymentId,
        final String status,
        final String errorCode,
        final String errorMessage,
        final String rawEvent,
        final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings)
                .insertInto(HYPERSWITCH_WEBHOOK_EVENTS,
                    HYPERSWITCH_WEBHOOK_EVENTS.KB_ACCOUNT_ID,
                    HYPERSWITCH_WEBHOOK_EVENTS.KB_PAYMENT_ID,
                    HYPERSWITCH_WEBHOOK_EVENTS.KB_PAYMENT_TRANSACTION_ID,
                    HYPERSWITCH_WEBHOOK_EVENTS.KB_TENANT_ID,
                    HYPERSWITCH_WEBHOOK_EVENTS.HYPERSWITCH_EVENT_ID,
                    HYPERSWITCH_WEBHOOK_EVENTS.HYPERSWITCH_EVENT_TYPE,
                    HYPERSWITCH_WEBHOOK_EVENTS.HYPERSWITCH_PAYMENT_ID,
                    HYPERSWITCH_WEBHOOK_EVENTS.EVENT_STATUS,
                    HYPERSWITCH_WEBHOOK_EVENTS.ERROR_CODE,
                    HYPERSWITCH_WEBHOOK_EVENTS.ERROR_MESSAGE,
                    HYPERSWITCH_WEBHOOK_EVENTS.RAW_EVENT,
                    HYPERSWITCH_WEBHOOK_EVENTS.CREATED_DATE)
                .values(kbAccountId.toString(),
                    kbPaymentId.toString(),
                    kbPaymentTransactionId.toString(),
                    kbTenantId.toString(),
                    eventId,
                    eventType,
                    paymentId,
                    status,
                    errorCode,
                    errorMessage,
                    rawEvent,
                    toLocalDateTime(clock.getUTCNow()))
                .execute());
    }

    public List<HyperswitchWebhookEventsRecord> getWebhookEvents(
        final UUID kbPaymentId,
        final UUID kbTenantId) throws SQLException {
        return execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings)
                .selectFrom(HYPERSWITCH_WEBHOOK_EVENTS)
                .where(HYPERSWITCH_WEBHOOK_EVENTS.KB_PAYMENT_ID.equal(kbPaymentId.toString()))
                .and(HYPERSWITCH_WEBHOOK_EVENTS.KB_TENANT_ID.equal(kbTenantId.toString()))
                .orderBy(HYPERSWITCH_WEBHOOK_EVENTS.CREATED_DATE.desc())
                .fetch());
    }

    public void updatePaymentMethodClientSecret(final UUID kbPaymentMethodId,
                                                final String clientSecret,
                                                final UUID kbTenantId) throws SQLException {
        execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings)
                .update(HYPERSWITCH_PAYMENT_METHODS)
                .set(HYPERSWITCH_PAYMENT_METHODS.CLIENT_SECRET, clientSecret)
                .where(HYPERSWITCH_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID.equal(kbPaymentMethodId.toString()))
                .and(HYPERSWITCH_PAYMENT_METHODS.KB_TENANT_ID.equal(kbTenantId.toString()))
                .execute());
    }

    public boolean isEventProcessed(String eventId, UUID tenantId) throws SQLException {
        return execute(dataSource.getConnection(),
            conn -> DSL.using(conn, dialect, settings)
                .selectCount()
                .from(HYPERSWITCH_WEBHOOK_EVENTS)
                .where(HYPERSWITCH_WEBHOOK_EVENTS.HYPERSWITCH_EVENT_ID.equal(eventId))
                .and(HYPERSWITCH_WEBHOOK_EVENTS.KB_TENANT_ID.equal(tenantId.toString()))
                .fetchOne(0, Integer.class) > 0);
    }

}
