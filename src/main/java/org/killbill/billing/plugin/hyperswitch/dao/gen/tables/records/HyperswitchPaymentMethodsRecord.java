/*
 * This file is generated by jOOQ.
 */
package org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records;


import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record11;
import org.jooq.Row11;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchPaymentMethods;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class HyperswitchPaymentMethodsRecord extends UpdatableRecordImpl<HyperswitchPaymentMethodsRecord> implements Record11<ULong, String, String, String, String, Short, Short, String, LocalDateTime, LocalDateTime, String> {

    private static final long serialVersionUID = 1159142458;

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.record_id</code>.
     */
    public void setRecordId(ULong value) {
        set(0, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.record_id</code>.
     */
    public ULong getRecordId() {
        return (ULong) get(0);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.kb_account_id</code>.
     */
    public void setKbAccountId(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.kb_account_id</code>.
     */
    public String getKbAccountId() {
        return (String) get(1);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.kb_payment_method_id</code>.
     */
    public void setKbPaymentMethodId(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.kb_payment_method_id</code>.
     */
    public String getKbPaymentMethodId() {
        return (String) get(2);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.hyperswitch_id</code>.
     */
    public void setHyperswitchId(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.hyperswitch_id</code>.
     */
    public String getHyperswitchId() {
        return (String) get(3);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.is_default</code>.
     */
    public void setIsDefault(Short value) {
        set(4, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.is_default</code>.
     */
    public Short getIsDefault() {
        return (Short) get(4);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.client_secret</code>.
     */
    public void setClientSecret(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.client_secret</code>.
     */
    public String getClientSecret() {
        return (String) get(5);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.is_deleted</code>.
     */
    public void setIsDeleted(Short value) {
        set(6, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.is_deleted</code>.
     */
    public Short getIsDeleted() {
        return (Short) get(6);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.additional_data</code>.
     */
    public void setAdditionalData(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.additional_data</code>.
     */
    public String getAdditionalData() {
        return (String) get(6);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.created_date</code>.
     */
    public void setCreatedDate(LocalDateTime value) {
        set(7, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.created_date</code>.
     */
    public LocalDateTime getCreatedDate() {
        return (LocalDateTime) get(7);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.updated_date</code>.
     */
    public void setUpdatedDate(LocalDateTime value) {
        set(8, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.updated_date</code>.
     */
    public LocalDateTime getUpdatedDate() {
        return (LocalDateTime) get(8);
    }

    /**
     * Setter for <code>killbill.hyperswitch_payment_methods.kb_tenant_id</code>.
     */
    public void setKbTenantId(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>killbill.hyperswitch_payment_methods.kb_tenant_id</code>.
     */
    public String getKbTenantId() {
        return (String) get(9);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row11<ULong, String, String, String, String, Short, Short, String, LocalDateTime, LocalDateTime, String> fieldsRow() {
        return (Row11) super.fieldsRow();
    }

    @Override
    public Row11<ULong, String, String, String, String, Short, Short, String, LocalDateTime, LocalDateTime, String> valuesRow() {
        return (Row11) super.valuesRow();
    }

    @Override
    public Field<ULong> field1() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.RECORD_ID;
    }

    @Override
    public Field<String> field2() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.KB_ACCOUNT_ID;
    }

    @Override
    public Field<String> field3() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.KB_PAYMENT_METHOD_ID;
    }

    @Override
    public Field<String> field4() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.HYPERSWITCH_ID;
    }

    @Override
    public Field<String> field5() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.CLIENT_SECRET;
    }

    @Override
    public Field<Short> field6() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.IS_DEFAULT;
    }

    @Override
    public Field<Short> field7() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.IS_DELETED;
    }

    @Override
    public Field<String> field8() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.ADDITIONAL_DATA;
    }

    @Override
    public Field<LocalDateTime> field9() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.CREATED_DATE;
    }

    @Override
    public Field<LocalDateTime> field10() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.UPDATED_DATE;
    }

    @Override
    public Field<String> field11() {
        return HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.KB_TENANT_ID;
    }

    @Override
    public ULong component1() {
        return getRecordId();
    }

    @Override
    public String component2() {
        return getKbAccountId();
    }

    @Override
    public String component3() {
        return getKbPaymentMethodId();
    }

    @Override
    public String component4() {
        return getHyperswitchId();
    }

    @Override
    public String component5() {
        return getClientSecret();
    }

    @Override
    public Short component6() {
        return getIsDefault();
    }

    @Override
    public Short component7() {
        return getIsDeleted();
    }

    @Override
    public String component8() {
        return getAdditionalData();
    }

    @Override
    public LocalDateTime component9() {
        return getCreatedDate();
    }

    @Override
    public LocalDateTime component10() {
        return getUpdatedDate();
    }

    @Override
    public String component11() {
        return getKbTenantId();
    }

    @Override
    public ULong value1() {
        return getRecordId();
    }

    @Override
    public String value2() {
        return getKbAccountId();
    }

    @Override
    public String value3() {
        return getKbPaymentMethodId();
    }

    @Override
    public String value4() {
        return getHyperswitchId();
    }

    @Override 
    public String value5() {
        return getClientSecret();
    }

    @Override
    public Short value6() {
        return getIsDefault();
    }

    @Override
    public Short value7() {
        return getIsDeleted();
    }

    @Override
    public String value8() {
        return getAdditionalData();
    }

    @Override
    public LocalDateTime value9() {
        return getCreatedDate();
    }

    @Override
    public LocalDateTime value10() {
        return getUpdatedDate();
    }

    @Override
    public String value11() {
        return getKbTenantId();
    }

    @Override
    public HyperswitchPaymentMethodsRecord value1(ULong value) {
        setRecordId(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value2(String value) {
        setKbAccountId(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value3(String value) {
        setKbPaymentMethodId(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value4(String value) {
        setHyperswitchId(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value5(String value) {
        setClientSecret(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value6(Short value) {
        setIsDeleted(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value7(Short value) {
        setIsDeleted(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value8(String value) {
        setAdditionalData(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value9(LocalDateTime value) {
        setCreatedDate(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value10(LocalDateTime value) {
        setUpdatedDate(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord value11(String value) {
        setKbTenantId(value);
        return this;
    }

    @Override
    public HyperswitchPaymentMethodsRecord values(ULong value1, String value2, String value3, String value4, String value5, Short value6, Short value7, String value8, LocalDateTime value9, LocalDateTime value10, String value11) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached HyperswitchPaymentMethodsRecord
     */
    public HyperswitchPaymentMethodsRecord() {
        super(HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS);
    }

    /**
     * Create a detached, initialised HyperswitchPaymentMethodsRecord
     */
    public HyperswitchPaymentMethodsRecord(ULong recordId, String kbAccountId, String kbPaymentMethodId, String hyperswitchId, String clientSecret, Short isDefault, Short isDeleted, String additionalData, LocalDateTime createdDate, LocalDateTime updatedDate, String kbTenantId) {
        super(HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS);

        set(0, recordId);
        set(1, kbAccountId);
        set(2, kbPaymentMethodId);
        set(3, hyperswitchId);
        set(4, isDefault);
        set(5, isDeleted);
        set(6, additionalData);
        set(7, createdDate);
        set(8, updatedDate);
        set(9, kbTenantId);
    }
}
