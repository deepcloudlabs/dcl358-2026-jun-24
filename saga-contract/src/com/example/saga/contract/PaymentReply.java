package com.example.saga.contract;

import java.util.UUID;

public record PaymentReply(
        UUID messageId,
        UUID causationId,
        UUID sagaId,
        long orderId,
        PaymentStatus status,
        String reason
) {
    public PaymentReply {
        if (messageId == null || causationId == null || sagaId == null) {
            throw new IllegalArgumentException("Message, causation, and saga ids are required.");
        }
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order id must be positive.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Payment status is required.");
        }
    }
}
