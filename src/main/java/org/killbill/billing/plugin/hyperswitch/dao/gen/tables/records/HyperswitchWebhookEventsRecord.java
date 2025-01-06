package org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records;

import java.time.LocalDateTime;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.impl.UpdatableRecordImpl;
import org.jooq.types.ULong;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchWebhookEvents;

public class HyperswitchWebhookEventsRecord extends UpdatableRecordImpl<HyperswitchWebhookEventsRecord> {

    private static final long serialVersionUID = 1L;

    public void setRecordId(ULong value) {
        set(0, value);
    }

    public ULong getRecordId() {
        return (ULong) get(0);
    }

    public void setKbAccountId(String value) {
        set(1, value);
    }

    public String getKbAccountId() {
        return (String) get(1);
    }

    public void setKbTenantId(String value) {
        set(2, value);
    }

    public String getKbTenantId() {
        return (String) get(2);
    }

    public void setKbPaymentId(String value) {
        set(3, value);
    }

    public String getKbPaymentId() {
        return (String) get(3);
    }

    public void setKbPaymentTransactionId(String value) {
        set(4, value);
    }

    public String getKbPaymentTransactionId() {
        return (String) get(4);
    }

    public void setHyperswitchEventId(String value) {
        set(5, value);
    }

    public String getHyperswitchEventId() {
        return (String) get(5);
    }

    public void setHyperswitchEventType(String value) {
        set(6, value);
    }

    public String getHyperswitchEventType() {
        return (String) get(6);
    }

    public void setHyperswitchPaymentId(String value) {
        set(7, value);
    }

    public String getHyperswitchPaymentId() {
        return (String) get(7);
    }

    public void setEventStatus(String value) {
        set(8, value);
    }

    public String getEventStatus() {
        return (String) get(8);
    }

    public void setErrorCode(String value) {
        set(9, value);
    }

    public String getErrorCode() {
        return (String) get(9);
    }

    public void setErrorMessage(String value) {
        set(10, value);
    }

    public String getErrorMessage() {
        return (String) get(10);
    }

    public void setRawEvent(String value) {
        set(11, value);
    }

    public String getRawEvent() {
        return (String) get(11);
    }

    public void setCreatedDate(LocalDateTime value) {
        set(12, value);
    }

    public LocalDateTime getCreatedDate() {
        return (LocalDateTime) get(12);
    }

    public HyperswitchWebhookEventsRecord() {
        super(HyperswitchWebhookEvents.HYPERSWITCH_WEBHOOK_EVENTS);
    }

    @Override
    public Record1<ULong> key() {
        return (Record1) super.key();
    }
}
