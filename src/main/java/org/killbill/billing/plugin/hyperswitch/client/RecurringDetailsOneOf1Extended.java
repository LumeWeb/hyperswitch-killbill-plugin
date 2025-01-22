package org.killbill.billing.plugin.hyperswitch.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hyperswitch.client.model.NetworkTransactionIdAndCardDetails;
import com.hyperswitch.client.model.RecurringDetails;

@JsonPropertyOrder({"type", "data"})
@JsonTypeName("RecurringDetails_oneOf_1")
public class RecurringDetailsOneOf1Extended extends RecurringDetails {
    public static enum TypeEnum {
        PAYMENT_METHOD_ID("payment_method_id");

        private String value;

        private TypeEnum(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return this.value;
        }

        public String toString() {
            return String.valueOf(this.value);
        }

        @JsonCreator
        public static TypeEnum fromValue(String value) {
            TypeEnum[] var1 = values();
            for(TypeEnum b : var1) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    public static class StringWrapper extends NetworkTransactionIdAndCardDetails {
        private final String value;

        public StringWrapper(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Override
    public RecurringDetailsOneOf1Extended data(NetworkTransactionIdAndCardDetails data) {
        super.data(data);
        return this;
    }

    public RecurringDetailsOneOf1Extended data(String data) {
        super.data(new StringWrapper(data));
        return this;
    }

    // Add the type method
    public RecurringDetailsOneOf1Extended type(TypeEnum type) {
        super.type(RecurringDetails.TypeEnum.valueOf(type.getValue().toUpperCase()));
        return this;
    }
}
