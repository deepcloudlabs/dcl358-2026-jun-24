package com.example.cm.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import com.example.cm.dto.request.CustomerAddressRequest;
import com.example.cm.dto.response.ChangeCustomerAddressResponse;
import com.example.cm.service.CustomerService;

@RestController
@RequestMapping("/customers")
@RequestScope
@CrossOrigin
public class CustomerManagementRestController {
	private final CustomerService customerService;

	public CustomerManagementRestController(CustomerService customerService) {
		this.customerService = customerService;
	}

	@PostMapping("/{customerId}/address")
	public ChangeCustomerAddressResponse changeCustomerAddress(@PathVariable String customerId,
			@RequestBody CustomerAddressRequest address) {
		return customerService.updateAddress(customerId, address);
	}
}
