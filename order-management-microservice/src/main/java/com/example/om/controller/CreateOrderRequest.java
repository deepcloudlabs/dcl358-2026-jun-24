package com.example.om.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotEmpty List<@Valid OrderItemRequest> items
) {
}
