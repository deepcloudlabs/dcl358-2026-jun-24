package com.example.indicator.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TradeEventConsumerService {

	@KafkaListener(topics = {"trades"},groupId = "indicator-service")
	public void listenEvents(String event) {
		System.out.println(event);
	}
}
