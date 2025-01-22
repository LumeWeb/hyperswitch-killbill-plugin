package org.killbill.billing.plugin.hyperswitch.client;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.hyperswitch.client.model.RecurringDetails;
import com.hyperswitch.client.model.NetworkTransactionIdAndCardDetails;

@JsonTypeName("RecurringDetails_oneOf_1")
public class RecurringDetailsOneOf1Extended extends RecurringDetails {

    private String paymentMethodId;

    public RecurringDetailsOneOf1Extended setPaymentMethodId(String id) {
        this.paymentMethodId = id;
        // Just create a dummy wrapper since we need something
        super.data(new NetworkTransactionIdAndCardDetails());
        return this;
    }

    @Override
    public NetworkTransactionIdAndCardDetails getData() {
        // Override to return our payment method id instead
        return new NetworkTransactionIdAndCardDetails() {
            @Override
            public String toString() {
                return paymentMethodId;
            }
        };
    }
}
