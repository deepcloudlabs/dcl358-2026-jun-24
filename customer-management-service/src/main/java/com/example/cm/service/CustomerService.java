package com.example.cm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.cm.domain.Address;
import com.example.cm.dto.request.CustomerAddressRequest;
import com.example.cm.dto.response.ChangeCustomerAddressResponse;
import com.example.cm.event.CustomerAddressChangedEvent;

import tools.jackson.databind.ObjectMapper;

@Service
public class CustomerService {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final String customerEventsTopic;
	private final ObjectMapper objectMapper;
	
	public CustomerService(KafkaTemplate<String, String> kafkaTemplate, 
			@Value("${customerEventsTopic}") String customerEventsTopic, ObjectMapper objectMapper) {
		this.kafkaTemplate = kafkaTemplate;
		this.customerEventsTopic = customerEventsTopic;
		this.objectMapper = objectMapper;
	}

	// Retry x 3 -> Outbox Pattern
	// @Transactional
	public ChangeCustomerAddressResponse updateAddress(String customerId,CustomerAddressRequest newAddress) {
		// Successfully saved to the database
		// customerId -- database --> Aggregate --> updateAddress() --> update --> database
		var address = new Address(newAddress.line(), newAddress.city(), newAddress.country());
		var event = new CustomerAddressChangedEvent(customerId,address);
		var eventAsJson = objectMapper.writeValueAsString(event);
		kafkaTemplate.send(customerEventsTopic, customerId, eventAsJson)
		             .thenAcceptAsync(System.out::println);
		return new ChangeCustomerAddressResponse("success");
	}

}
