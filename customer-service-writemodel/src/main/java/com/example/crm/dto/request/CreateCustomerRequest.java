package com.example.crm.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank String identity,
        @NotBlank String fullname,
        @Valid @Size(max = 3) List<Address> addresses,
        @Valid @Size(max = 3) List<Phone> phones
) {
}
