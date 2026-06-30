package com.example.om.controller;

import java.math.BigDecimal;

import com.example.om.domain.OrderItem;

public record OrderItemResponse(
        String sku,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getSku(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal());
    }
}
