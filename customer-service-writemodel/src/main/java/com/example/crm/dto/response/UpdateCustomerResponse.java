package com.example.crm.dto.response;

import java.util.List;

public record UpdateCustomerResponse(String status, String identity, List<Long> sequenceNumbers) {
}
