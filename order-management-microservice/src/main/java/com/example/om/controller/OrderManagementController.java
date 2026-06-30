package com.example.om.controller;

import java.net.URI;
import java.util.NoSuchElementException;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.om.repository.OrderRepository;
import com.example.om.service.OrderSagaCoordinator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
@CrossOrigin
@Validated
public class OrderManagementController {
    private final OrderSagaCoordinator sagaCoordinator;
    private final OrderRepository orderRepository;

    public OrderManagementController(OrderSagaCoordinator sagaCoordinator, OrderRepository orderRepository) {
        this.sagaCoordinator = sagaCoordinator;
        this.orderRepository = orderRepository;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        var order = sagaCoordinator.createOrder(request);
        return ResponseEntity.created(URI.create("/orders/" + order.getOrderId()))
                .body(OrderResponse.from(order));
    }

    @GetMapping("/{orderId}")
    public OrderResponse findOrder(@PathVariable long orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Order %d was not found.".formatted(orderId)));
    }
}
