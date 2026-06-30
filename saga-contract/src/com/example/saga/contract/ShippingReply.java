package com.example.saga.contract;

import java.util.UUID;

public record ShippingReply(
        UUID messageId,
        UUID causationId,
        UUID sagaId,
        long orderId,
        ShippingStatus status,
        String reason
) {
    public ShippingReply {
        if (messageId == null || causationId == null || sagaId == null) {
            throw new IllegalArgumentException("Message, causation, and saga ids are required.");
        }
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order id must be positive.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Shipping status is required.");
        }
    }
}
