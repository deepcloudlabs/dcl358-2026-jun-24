package com.example.saga.contract;

import java.util.List;
import java.util.UUID;

public record ShippingCommand(
        UUID messageId,
        UUID sagaId,
        long orderId,
        String customerId,
        List<OrderItemMessage> items,
        ShippingCommandType commandType
) {
    public ShippingCommand {
        if (messageId == null) {
            throw new IllegalArgumentException("Message id is required.");
        }
        if (sagaId == null) {
            throw new IllegalArgumentException("Saga id is required.");
        }
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order id must be positive.");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer id is required.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Shipping command requires at least one item.");
        }
        items = List.copyOf(items);
        if (commandType == null) {
            throw new IllegalArgumentException("Shipping command type is required.");
        }
    }
}
