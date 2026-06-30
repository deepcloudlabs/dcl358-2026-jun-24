package com.example.saga.contract;

public final class SagaTopics {
    public static final String ORDER_PAYMENT_COMMAND = "orders.payment.commands";
    public static final String ORDER_PAYMENT_REPLY = "orders.payment.replies";
    public static final String ORDER_INVENTORY_COMMAND = "orders.inventory.commands";
    public static final String ORDER_INVENTORY_REPLY = "orders.inventory.replies";
    public static final String ORDER_SHIPPING_COMMAND = "orders.shipping.commands";
    public static final String ORDER_SHIPPING_REPLY = "orders.shipping.replies";
    public static final String ORDER_EVENTS = "orders.events";

    private SagaTopics() {
    }
}
