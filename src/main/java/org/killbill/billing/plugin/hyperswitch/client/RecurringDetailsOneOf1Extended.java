package org.killbill.billing.plugin.hyperswitch.client;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.hyperswitch.client.model.RecurringDetails;
import com.hyperswitch.client.model.NetworkTransactionIdAndCardDetails;

@JsonTypeName("payment_method_id")  // Changed this to match the expected variant
public class RecurringDetailsOneOf1Extended extends RecurringDetails {

    private String paymentMethodId;

    public RecurringDetailsOneOf1Extended setPaymentMethodId(String id) {
        this.paymentMethodId = id;
        super.data(new NetworkTransactionIdAndCardDetails());
        return this;
    }

    @Override
    public NetworkTransactionIdAndCardDetails getData() {
        return new NetworkTransactionIdAndCardDetails() {
            @Override
            public String toString() {
                return paymentMethodId;
            }
        };
    }
}
