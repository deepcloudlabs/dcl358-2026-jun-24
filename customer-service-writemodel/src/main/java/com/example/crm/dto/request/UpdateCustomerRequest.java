package com.example.crm.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        String fullname,
        @Valid @Size(max = 3) List<Address> addresses,
        @Valid @Size(max = 3) List<Phone> phones
) {
}
