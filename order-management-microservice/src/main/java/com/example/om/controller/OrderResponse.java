package com.example.om.controller;

import java.math.BigDecimal;
import java.util.List;

import com.example.om.domain.Order;
import com.example.om.domain.OrderStatus;

public record OrderResponse(
        long orderId,
        String sagaId,
        String customerId,
        OrderStatus status,
        BigDecimal total,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getSagaId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotal(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
