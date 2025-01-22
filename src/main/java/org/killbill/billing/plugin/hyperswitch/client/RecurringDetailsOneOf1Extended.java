package org.killbill.billing.plugin.hyperswitch.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hyperswitch.client.model.RecurringDetails;
import com.hyperswitch.client.model.NetworkTransactionIdAndCardDetails;

@JsonTypeName("payment_method_id")
public class RecurringDetailsOneOf1Extended extends RecurringDetails {

    private static class StringOnlyData extends NetworkTransactionIdAndCardDetails {
        private final String value;

        StringOnlyData(String value) {
            this.value = value;
        }

        // Override all serialization to just output the string
        @JsonValue  // This tells Jackson to use this value directly
        public String getValue() {
            return value;
        }
    }

    public RecurringDetailsOneOf1Extended setPaymentMethodId(String id) {
        super.data(new StringOnlyData(id));
        return this;
    }
}
