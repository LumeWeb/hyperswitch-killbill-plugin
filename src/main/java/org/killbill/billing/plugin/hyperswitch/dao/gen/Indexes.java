/*
 * This file is generated by jOOQ.
 */
package org.killbill.billing.plugin.hyperswitch.dao.gen;


import org.jooq.Index;
import org.jooq.OrderField;
import org.jooq.impl.Internal;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchHppRequests;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchPaymentMethods;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchResponses;
import org.killbill.billing.plugin.hyperswitch.dao.gen.tables.HyperswitchWebhookEvents;

/**
 * A class modelling indexes of tables of the <code>killbill</code> schema.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Indexes {

    // -------------------------------------------------------------------------
    // INDEX definitions
    // -------------------------------------------------------------------------

    public static final Index HYPERSWITCH_HPP_REQUESTS_HYPERSWITCH_HPP_REQUESTS_KB_ACCOUNT_ID = Indexes0.HYPERSWITCH_HPP_REQUESTS_HYPERSWITCH_HPP_REQUESTS_KB_ACCOUNT_ID;
    public static final Index HYPERSWITCH_HPP_REQUESTS_HYPERSWITCH_HPP_REQUESTS_KB_PAYMENT_TRANSACTION_ID = Indexes0.HYPERSWITCH_HPP_REQUESTS_HYPERSWITCH_HPP_REQUESTS_KB_PAYMENT_TRANSACTION_ID;
    public static final Index HYPERSWITCH_PAYMENT_METHODS_HYPERSWITCH_PAYMENT_METHODS_HYPERSWITCH_ID = Indexes0.HYPERSWITCH_PAYMENT_METHODS_HYPERSWITCH_PAYMENT_METHODS_HYPERSWITCH_ID;
    public static final Index HYPERSWITCH_RESPONSES_HYPERSWITCH_RESPONSES_KB_PAYMENT_ID = Indexes0.HYPERSWITCH_RESPONSES_HYPERSWITCH_RESPONSES_KB_PAYMENT_ID;
    public static final Index HYPERSWITCH_RESPONSES_HYPERSWITCH_RESPONSES_KB_PAYMENT_TRANSACTION_ID = Indexes0.HYPERSWITCH_RESPONSES_HYPERSWITCH_RESPONSES_KB_PAYMENT_TRANSACTION_ID;
    public static final Index HYPERSWITCH_RESPONSES_HYPERSWITCH_RESPONSES_PAYMENT_ATTEMPT_ID = Indexes0.HYPERSWITCH_RESPONSES_HYPERSWITCH_RESPONSES_PAYMENT_ATTEMPT_ID;
    public static final Index HYPERSWITCH_WEBHOOK_EVENTS_PAYMENT = Indexes0.HYPERSWITCH_WEBHOOK_EVENTS_PAYMENT;
    public static final Index HYPERSWITCH_WEBHOOK_EVENTS_TRANSACTION = Indexes0.HYPERSWITCH_WEBHOOK_EVENTS_TRANSACTION;
    public static final Index HYPERSWITCH_WEBHOOK_EVENTS_HYPERSWITCH_ID = Indexes0.HYPERSWITCH_WEBHOOK_EVENTS_HYPERSWITCH_ID;

    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class Indexes0 {
        public static Index HYPERSWITCH_HPP_REQUESTS_HYPERSWITCH_HPP_REQUESTS_KB_ACCOUNT_ID = Internal.createIndex("hyperswitch_hpp_requests_kb_account_id", HyperswitchHppRequests.HYPERSWITCH_HPP_REQUESTS, new OrderField[] { HyperswitchHppRequests.HYPERSWITCH_HPP_REQUESTS.KB_ACCOUNT_ID }, false);
        public static Index HYPERSWITCH_HPP_REQUESTS_HYPERSWITCH_HPP_REQUESTS_KB_PAYMENT_TRANSACTION_ID = Internal.createIndex("hyperswitch_hpp_requests_kb_payment_transaction_id", HyperswitchHppRequests.HYPERSWITCH_HPP_REQUESTS, new OrderField[] { HyperswitchHppRequests.HYPERSWITCH_HPP_REQUESTS.KB_PAYMENT_TRANSACTION_ID }, false);
        public static Index HYPERSWITCH_PAYMENT_METHODS_HYPERSWITCH_PAYMENT_METHODS_HYPERSWITCH_ID = Internal.createIndex("hyperswitch_payment_methods_hyperswitch_id", HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS, new OrderField[] { HyperswitchPaymentMethods.HYPERSWITCH_PAYMENT_METHODS.HYPERSWITCH_ID }, false);
        public static Index HYPERSWITCH_RESPONSES_HYPERSWITCH_RESPONSES_KB_PAYMENT_ID = Internal.createIndex("hyperswitch_responses_kb_payment_id", HyperswitchResponses.HYPERSWITCH_RESPONSES, new OrderField[] { HyperswitchResponses.HYPERSWITCH_RESPONSES.KB_PAYMENT_ID }, false);
        public static Index HYPERSWITCH_RESPONSES_HYPERSWITCH_RESPONSES_KB_PAYMENT_TRANSACTION_ID = Internal.createIndex("hyperswitch_responses_kb_payment_transaction_id", HyperswitchResponses.HYPERSWITCH_RESPONSES, new OrderField[] { HyperswitchResponses.HYPERSWITCH_RESPONSES.KB_PAYMENT_TRANSACTION_ID }, false);
        public static Index HYPERSWITCH_RESPONSES_HYPERSWITCH_RESPONSES_PAYMENT_ATTEMPT_ID = Internal.createIndex("hyperswitch_responses_hyperswitch_id", HyperswitchResponses.HYPERSWITCH_RESPONSES, new OrderField[] { HyperswitchResponses.HYPERSWITCH_RESPONSES.PAYMENT_ATTEMPT_ID }, false);
        public static Index HYPERSWITCH_WEBHOOK_EVENTS_PAYMENT = Internal.createIndex("hyperswitch_webhook_events_payment", HyperswitchWebhookEvents.HYPERSWITCH_WEBHOOK_EVENTS, new OrderField[] {HyperswitchWebhookEvents.HYPERSWITCH_WEBHOOK_EVENTS.KB_PAYMENT_ID }, false);
        public static Index HYPERSWITCH_WEBHOOK_EVENTS_TRANSACTION = Internal.createIndex("hyperswitch_webhook_events_transaction", HyperswitchWebhookEvents.HYPERSWITCH_WEBHOOK_EVENTS, new OrderField[] { HyperswitchWebhookEvents.HYPERSWITCH_WEBHOOK_EVENTS.KB_PAYMENT_TRANSACTION_ID }, false);
        public static Index HYPERSWITCH_WEBHOOK_EVENTS_HYPERSWITCH_ID = Internal.createIndex("hyperswitch_webhook_events_hyperswitch_id", HyperswitchWebhookEvents.HYPERSWITCH_WEBHOOK_EVENTS, new OrderField[] { HyperswitchWebhookEvents.HYPERSWITCH_WEBHOOK_EVENTS.HYPERSWITCH_PAYMENT_ID }, false);
    }
}
