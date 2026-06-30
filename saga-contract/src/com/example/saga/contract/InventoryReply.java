package com.example.saga.contract;

import java.util.UUID;

public record InventoryReply(
        UUID messageId,
        UUID causationId,
        UUID sagaId,
        long orderId,
        InventoryStatus status,
        String reason
) {
    public InventoryReply {
        if (messageId == null || causationId == null || sagaId == null) {
            throw new IllegalArgumentException("Message, causation, and saga ids are required.");
        }
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order id must be positive.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Inventory status is required.");
        }
    }
}
