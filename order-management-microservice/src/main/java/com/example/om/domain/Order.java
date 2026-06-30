package com.example.om.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false, unique = true, length = 36)
    private String sagaId;

    @Column(nullable = false, length = 64)
    private String customerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderStatus status;

    protected Order() {
    }

    public static Order create(String customerId, List<OrderItem> items) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer id must not be blank.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order requires at least one item.");
        }
        var order = new Order();
        order.sagaId = UUID.randomUUID().toString();
        order.customerId = customerId.strip();
        order.status = OrderStatus.CREATED;
        items.forEach(order::addItem);
        return order;
    }

    public void addItem(OrderItem item) {
        item.attachTo(this);
        items.add(item);
        total = total.add(item.getLineTotal());
    }

    public boolean is(OrderStatus expected) {
        return status == expected;
    }

    public void mark(OrderStatus nextStatus) {
        if (nextStatus == null) {
            throw new IllegalArgumentException("Order status must not be null.");
        }
        status = nextStatus;
    }

    public Long getOrderId() {
        return orderId;
    }

    public UUID getSagaIdAsUuid() {
        return UUID.fromString(sagaId);
    }

    public String getSagaId() {
        return sagaId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
