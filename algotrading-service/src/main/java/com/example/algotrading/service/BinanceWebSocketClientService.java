package com.example.algotrading.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import com.example.algotrading.event.TradeEvent;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

@Service
public class BinanceWebSocketClientService implements WebSocketHandler{
	
	private final WebSocketClient webSocketClient;
	private final ObjectMapper objectMapper;
	private final KafkaTradeEventProducer producer;
	
	public BinanceWebSocketClientService(WebSocketClient webSocketClient, ObjectMapper objectMapper, KafkaTradeEventProducer producer) {
		this.webSocketClient = webSocketClient;
		this.objectMapper = objectMapper;
		this.producer = producer;
	}

	@PostConstruct
	public void connect() {
		webSocketClient.execute(this, "wss://stream.binance.com:9443/ws/btcusdt@trade");
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.err.println("Connected to the binance server!");
		
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		var tradeEventAsJson = message.getPayload().toString();
		var tradeEvent = objectMapper.readValue(tradeEventAsJson, TradeEvent.class);
		System.out.println(tradeEvent);
		producer.send(tradeEvent);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean supportsPartialMessages() {
		// TODO Auto-generated method stub
		return false;
	}
}
