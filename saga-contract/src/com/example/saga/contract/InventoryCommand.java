package com.example.saga.contract;

import java.util.List;
import java.util.UUID;

public record InventoryCommand(
        UUID messageId,
        UUID sagaId,
        long orderId,
        List<OrderItemMessage> items,
        InventoryCommandType commandType
) {
    public InventoryCommand {
        if (messageId == null) {
            throw new IllegalArgumentException("Message id is required.");
        }
        if (sagaId == null) {
            throw new IllegalArgumentException("Saga id is required.");
        }
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order id must be positive.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Inventory command requires at least one item.");
        }
        items = List.copyOf(items);
        if (commandType == null) {
            throw new IllegalArgumentException("Inventory command type is required.");
        }
    }
}
