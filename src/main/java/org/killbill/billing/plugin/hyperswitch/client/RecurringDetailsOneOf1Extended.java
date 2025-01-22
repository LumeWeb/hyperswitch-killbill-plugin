package org.killbill.billing.plugin.hyperswitch.client;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hyperswitch.client.model.RecurringDetails;
import com.hyperswitch.client.model.NetworkTransactionIdAndCardDetails;

@JsonTypeName("payment_method_id")
public class RecurringDetailsOneOf1Extended extends RecurringDetails {

    @JsonProperty("data")
    private String paymentMethodId;

    public RecurringDetailsOneOf1Extended setPaymentMethodId(String id) {
        this.paymentMethodId = id;
        return this;
    }

    @Override
    @JsonProperty("data")
    public NetworkTransactionIdAndCardDetails getData() {
        // This still needs to exist for inheritance, but won't be used in serialization
        return null;
    }

    // Add this to override the parent's serialization
    @JsonProperty("data")
    public String getStringData() {
        return paymentMethodId;
    }
}
