package com.example.saga.contract;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCommand(
        UUID messageId,
        UUID sagaId,
        long orderId,
        String customerId,
        BigDecimal amount,
        PaymentCommandType commandType
) {
    public PaymentCommand {
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
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (commandType == null) {
            throw new IllegalArgumentException("Payment command type is required.");
        }
    }
}
