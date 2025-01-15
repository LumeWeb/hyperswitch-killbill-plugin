/*
 * This file is generated by jOOQ.
 */
package org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchResponses;

/** This class is generated by jOOQ. */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class HyperswitchResponsesRecord extends UpdatableRecordImpl<HyperswitchResponsesRecord> {

    private static final long serialVersionUID = 1L;

    /** Setter for <code>killbill.hyperswitch_responses.record_id</code>. */
    public void setRecordId(ULong value) {
        set(0, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.record_id</code>. */
    public ULong getRecordId() {
        return (ULong) get(0);
    }

    /** Setter for <code>killbill.hyperswitch_responses.kb_account_id</code>. */
    public void setKbAccountId(String value) {
        set(1, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.kb_account_id</code>. */
    public String getKbAccountId() {
        return (String) get(1);
    }

    /** Setter for <code>killbill.hyperswitch_responses.kb_payment_id</code>. */
    public void setKbPaymentId(String value) {
        set(2, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.kb_payment_id</code>. */
    public String getKbPaymentId() {
        return (String) get(2);
    }

    /**
     * Setter for
     * <code>killbill.hyperswitch_responses.kb_payment_transaction_id</code>.
     */
    public void setKbPaymentTransactionId(String value) {
        set(3, value);
    }

    /**
     * Getter for
     * <code>killbill.hyperswitch_responses.kb_payment_transaction_id</code>.
     */
    public String getKbPaymentTransactionId() {
        return (String) get(3);
    }

    /**
     * Setter for <code>killbill.hyperswitch_responses.kb_payment_method_id</code>.
     */
    public void setKbPaymentMethodId(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_responses.kb_payment_method_id</code>.
     */
    public String getKbPaymentMethodId() {
        return (String) get(4);
    }

    /** Setter for <code>killbill.hyperswitch_responses.transaction_type</code>. */
    public void setTransactionType(String value) {
        set(5, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.transaction_type</code>. */
    public String getTransactionType() {
        return (String) get(5);
    }

    /** Setter for <code>killbill.hyperswitch_responses.amount</code>. */
    public void setAmount(BigDecimal value) {
        set(6, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.amount</code>. */
    public BigDecimal getAmount() {
        return (BigDecimal) get(6);
    }

    /** Setter for <code>killbill.hyperswitch_responses.currency</code>. */
    public void setCurrency(String value) {
        set(7, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.currency</code>. */
    public String getCurrency() {
        return (String) get(7);
    }

    /** Setter for <code>killbill.hyperswitch_responses.psp_reference</code>. */
    public void setPaymentAttemptId(String value) {
        set(7, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.psp_reference</code>. */
    public String getPaymentAttemptId() {
        return (String) get(7);
    }

    /** Setter for <code>killbill.hyperswitch_responses.result_code</code>. */
    public void setErrorMessage(String value) {
        set(8, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.result_code</code>. */
    public String getErrorMessage() {
        return (String) get(8);
    }

    /** Setter for <code>killbill.hyperswitch_responses.refusal_reason</code>. */
    public void setErrorCode(String value) {
        set(9, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.refusal_reason</code>. */
    public String getErrorCode() {
        return (String) get(9);
    }

    /** Setter for <code>killbill.hyperswitch_responses.additional_data</code>. */
    public void setAdditionalData(String value) {
        set(10, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.additional_data</code>. */
    public String getAdditionalData() {
        return (String) get(10);
    }

    /** Setter for <code>killbill.hyperswitch_responses.created_date</code>. */
    public void setCreatedDate(LocalDateTime value) {
        set(11, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.created_date</code>. */
    public LocalDateTime getCreatedDate() {
        return (LocalDateTime) get(11);
    }

    /** Setter for <code>killbill.hyperswitch_responses.kb_tenant_id</code>. */
    public void setKbTenantId(String value) {
        set(12, value);
    }

    /** Getter for <code>killbill.hyperswitch_responses.kb_tenant_id</code>. */
    public String getKbTenantId() {
        return (String) get(12);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /** Create a detached HyperswitchResponsesRecord */
    public HyperswitchResponsesRecord() {
        super(HyperswitchResponses.HYPERSWITCH_RESPONSES);
    }

    /** Create a detached, initialised HyperswitchResponsesRecord */
    public HyperswitchResponsesRecord(
            ULong recordId,
            String kbAccountId,
            String kbPaymentId,
            String kbPaymentTransactionId,
            String transactionType,
            BigDecimal amount,
            String currency,
            String paymentAttemptId,
            String errorMessage,
            String errorCode,
            String additionalData,
            LocalDateTime createdDate,
            String kbTenantId) {
        super(HyperswitchResponses.HYPERSWITCH_RESPONSES);

        setRecordId(recordId);
        setKbAccountId(kbAccountId);
        setKbPaymentId(kbPaymentId);
        setKbPaymentTransactionId(kbPaymentTransactionId);
        setTransactionType(transactionType);
        setAmount(amount);
        setCurrency(currency);
        setPaymentAttemptId(paymentAttemptId);
        setErrorMessage(errorMessage);
        setErrorCode(errorCode);
        setAdditionalData(additionalData);
        setCreatedDate(createdDate);
        setKbTenantId(kbTenantId);
    }
}
