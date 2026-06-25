package com.example.algotrading.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.algotrading.event.TradeEvent;
import com.example.algotrading.reporitory.TradeDocumentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
public class KafkaTradeEventProducer {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final String topicName;
	private final TradeDocumentRepository tradeEventRepository;
	
	public KafkaTradeEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper,
			@Value("${topicName}") String topicName, TradeDocumentRepository tradeEventRepository) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.topicName = topicName;
		this.tradeEventRepository = tradeEventRepository;
	}

	@Retry(name = "kafka", fallbackMethod = "saveEventToOutbox")
	public void send(TradeEvent tradeEvent) throws JsonProcessingException {
		var document = TradeEvent.toDocument(tradeEvent);
		var tradeDocumentAsJson = objectMapper.writeValueAsString(document);
		kafkaTemplate.send(topicName, tradeEvent.symbol(), tradeDocumentAsJson);
	}

	@Transactional
	public void saveEventToOutbox(TradeEvent tradeEvent, Throwable t) {
		var document = TradeEvent.toDocument(tradeEvent);
		tradeEventRepository.save(document);
	}
}
