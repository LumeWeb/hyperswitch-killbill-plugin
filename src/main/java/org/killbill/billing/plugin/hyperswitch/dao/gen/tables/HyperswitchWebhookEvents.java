package org.killbill.billing.plugin.hyperswitch.dao.gen.tables;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;
import org.jooq.types.ULong;
import org.killbill.billing.plugin.hyperswitch.dao.gen.Indexes;
import org.killbill.billing.plugin.hyperswitch.dao.gen.Keys;
import org.killbill.billing.plugin.hyperswitch.dao.gen.Killbill;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.records.HyperswitchWebhookEventsRecord;

public class HyperswitchWebhookEvents extends TableImpl<HyperswitchWebhookEventsRecord> {

    private static final long serialVersionUID = 1L;

    public static final HyperswitchWebhookEvents HYPERSWITCH_WEBHOOK_EVENTS = new HyperswitchWebhookEvents();

    @Override
    public Class<HyperswitchWebhookEventsRecord> getRecordType() {
        return HyperswitchWebhookEventsRecord.class;
    }

    public final TableField<HyperswitchWebhookEventsRecord, ULong> RECORD_ID = createField(DSL.name("record_id"),
                                                                                           SQLDataType.BIGINTUNSIGNED.nullable(false).identity(true), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> KB_ACCOUNT_ID = createField(DSL.name("kb_account_id"),
        SQLDataType.CHAR(36).nullable(false), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> KB_TENANT_ID = createField(DSL.name("kb_tenant_id"),
        SQLDataType.CHAR(36).nullable(false), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> KB_PAYMENT_ID = createField(DSL.name("kb_payment_id"),
        SQLDataType.CHAR(36).nullable(false), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> KB_PAYMENT_TRANSACTION_ID = createField(
        DSL.name("kb_payment_transaction_id"), SQLDataType.CHAR(36).nullable(false), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> HYPERSWITCH_EVENT_ID = createField(
        DSL.name("hyperswitch_event_id"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> HYPERSWITCH_EVENT_TYPE = createField(
        DSL.name("hyperswitch_event_type"), SQLDataType.VARCHAR(64).nullable(false), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> HYPERSWITCH_PAYMENT_ID = createField(
        DSL.name("hyperswitch_payment_id"), SQLDataType.VARCHAR(255).nullable(false), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> EVENT_STATUS = createField(
        DSL.name("event_status"), SQLDataType.VARCHAR(32).nullable(false), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> ERROR_CODE = createField(
        DSL.name("error_code"), SQLDataType.VARCHAR(64), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> ERROR_MESSAGE = createField(
        DSL.name("error_message"), SQLDataType.VARCHAR(255), this, "");

    public final TableField<HyperswitchWebhookEventsRecord, String> RAW_EVENT = createField(
        DSL.name("raw_event"), SQLDataType.CLOB, this, "");

    public final TableField<HyperswitchWebhookEventsRecord, LocalDateTime> CREATED_DATE = createField(
        DSL.name("created_date"), SQLDataType.LOCALDATETIME(0).nullable(false), this, "");

    private HyperswitchWebhookEvents(Name alias, Table<HyperswitchWebhookEventsRecord> aliased) {
        this(alias, aliased, null);
    }

    private HyperswitchWebhookEvents(Name alias, Table<HyperswitchWebhookEventsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public HyperswitchWebhookEvents() {
        this(DSL.name("hyperswitch_webhook_events"), null);
    }

    @Override
    public Schema getSchema() {
        return Killbill.KILLBILL;
    }

    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(
            Indexes.HYPERSWITCH_WEBHOOK_EVENTS_PAYMENT,
            Indexes.HYPERSWITCH_WEBHOOK_EVENTS_TRANSACTION,
            Indexes.HYPERSWITCH_WEBHOOK_EVENTS_HYPERSWITCH_ID);
    }

    @Override
    public Identity<HyperswitchWebhookEventsRecord, ULong> getIdentity() {
        return Keys.IDENTITY_HYPERSWITCH_WEBHOOK_EVENTS;
    }

    @Override
    public UniqueKey<HyperswitchWebhookEventsRecord> getPrimaryKey() {
        return Keys.KEY_HYPERSWITCH_WEBHOOK_EVENTS_PRIMARY;
    }

    @Override
    public HyperswitchWebhookEvents as(String alias) {
        return new HyperswitchWebhookEvents(DSL.name(alias), this);
    }

    @Override
    public HyperswitchWebhookEvents rename(String name) {
        return new HyperswitchWebhookEvents(DSL.name(name), null);
    }
}
