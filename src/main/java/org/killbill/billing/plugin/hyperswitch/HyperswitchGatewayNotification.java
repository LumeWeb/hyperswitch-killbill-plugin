package org.killbill.billing.plugin.hyperswitch;

import org.killbill.billing.payment.api.PluginProperty;
import org.killbill.billing.payment.plugin.api.GatewayNotification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HyperswitchGatewayNotification implements GatewayNotification {
    private final UUID kbPaymentId;

    public HyperswitchGatewayNotification(UUID kbPaymentId) {
        this.kbPaymentId = kbPaymentId;
    }

    @Override
    public UUID getKbPaymentId() {
        return kbPaymentId;
    }

    @Override
    public int getStatus() {
        return 200;
    }

    @Override
    public String getEntity() {
        return null;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return Map.of();
    }

    @Override
    public List<PluginProperty> getProperties() {
        return List.of();
    }
}
