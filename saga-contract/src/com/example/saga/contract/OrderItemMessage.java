package com.example.saga.contract;

import java.math.BigDecimal;

public record OrderItemMessage(String sku, BigDecimal unitPrice, int quantity) {
    public OrderItemMessage {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank.");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
    }

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
