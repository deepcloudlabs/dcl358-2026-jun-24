package com.example.om.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.om.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findBySagaId(String sagaId);
}
