package org.killbill.billing.plugin.hyperswitch.client;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.hyperswitch.client.model.RecurringDetails;
import com.hyperswitch.client.model.NetworkTransactionIdAndCardDetails;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

@JsonTypeName("payment_method_id")
@JsonSerialize(using = RecurringDetailsOneOf1Extended.CustomSerializer.class)
public class RecurringDetailsOneOf1Extended extends RecurringDetails {

    private String paymentMethodId;

    public RecurringDetailsOneOf1Extended setPaymentMethodId(String id) {
        this.paymentMethodId = id;
        return this;
    }

    @Override
    public NetworkTransactionIdAndCardDetails getData() {
        return null;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public static class CustomSerializer extends StdSerializer<RecurringDetailsOneOf1Extended> {
        public CustomSerializer() {
            super(RecurringDetailsOneOf1Extended.class);
        }

        @Override
        public void serialize(RecurringDetailsOneOf1Extended value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
            gen.writeStartObject();
            gen.writeStringField("type", "payment_method_id");
            gen.writeStringField("data", value.getPaymentMethodId());
            gen.writeEndObject();
        }
    }
}
