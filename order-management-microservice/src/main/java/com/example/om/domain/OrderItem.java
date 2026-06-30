package com.example.om.domain;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    protected OrderItem() {
    }

    public OrderItem(String sku, BigDecimal unitPrice, int quantity) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU must not be blank.");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        this.sku = sku.strip();
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getItemId() {
        return itemId;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public Order getOrder() {
        return order;
    }

    void attachTo(Order order) {
        this.order = Objects.requireNonNull(order, "order");
    }
}
