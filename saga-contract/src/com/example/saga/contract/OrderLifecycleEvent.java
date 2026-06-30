package com.example.saga.contract;

import java.time.Instant;
import java.util.UUID;

public record OrderLifecycleEvent(
        UUID messageId,
        UUID sagaId,
        long orderId,
        OrderLifecycleEventType eventType,
        String status,
        String detail,
        Instant occurredAt
) {
    public OrderLifecycleEvent {
        if (messageId == null || sagaId == null) {
            throw new IllegalArgumentException("Message and saga ids are required.");
        }
        if (orderId <= 0) {
            throw new IllegalArgumentException("Order id must be positive.");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is required.");
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
