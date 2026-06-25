package com.example.iq.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CustomerEventsConsumerService {

	@KafkaListener(topics = {"customer-events"},groupId = "insurance-quoting")
	public void listenCustomerEvents(String event) {
		System.out.println(event);
	}
}
