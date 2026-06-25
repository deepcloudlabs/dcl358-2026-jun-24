package com.example.algotrading.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import com.example.algotrading.event.StreamEvent;

import jakarta.annotation.PostConstruct;
import tools.jackson.databind.ObjectMapper;

@Service
public class BinanceWebSocketClientService implements WebSocketHandler {
	private static final int STREAM_BATCH_SIZE = 5000;

	private static final String EXCHANGE_INFO_URL = "https://api.binance.com/api/v3/exchangeInfo?symbolStatus=TRADING";

	private static final String BINANCE_COMBINED_STREAM_URL = "wss://stream.binance.com:9443/stream?streams=%s";
	private final RestClient restClient;

	private final WebSocketClient webSocketClient;
	private final ObjectMapper objectMapper;
	private final KafkaTradeEventProducer producer;

	public BinanceWebSocketClientService(WebSocketClient webSocketClient, ObjectMapper objectMapper,
			KafkaTradeEventProducer producer) {
		this.restClient = RestClient.create();
		;
		this.webSocketClient = webSocketClient;
		this.objectMapper = objectMapper;
		this.producer = producer;
	}

	@PostConstruct
	public void connect() {
		var symbols = fetchAllTradingSymbols();

		if (symbols.isEmpty()) {
			throw new IllegalStateException("No TRADING symbols returned from Binance exchangeInfo.");
		}

		var streams = symbols.stream().map(this::toTradeStreamName).toList();

		var batches = partition(streams, STREAM_BATCH_SIZE);

		for (var batch : batches) {
			var combinedStreams = String.join("/", batch);
			var url = BINANCE_COMBINED_STREAM_URL.formatted(combinedStreams);
			System.out.println(url);

			webSocketClient.execute(this, url);
		}

		System.out.printf("Subscribed to %d Binance trade streams using %d WebSocket connection(s).%n", streams.size(),
				batches.size());
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		System.err.println("Connected to the binance server!");

	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		var tradeEventAsJson = message.getPayload().toString();
		System.out.println(tradeEventAsJson);
		var streamEvent = objectMapper.readValue(tradeEventAsJson, StreamEvent.class);
		var tradeEvent = streamEvent.data();
		System.out.println(tradeEvent);
		producer.send(tradeEvent);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		System.err.println("Error has occured in websocket: %s".formatted(exception.getMessage()));
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		System.err.println("Connection is closed.");
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

	private List<String> fetchAllTradingSymbols() {
		var exchangeInfo = restClient.get().uri(EXCHANGE_INFO_URL).retrieve().body(BinanceExchangeInfo.class);

		if (exchangeInfo == null || exchangeInfo.symbols() == null) {
			return List.of();
		}

		return exchangeInfo.symbols().stream().filter(Objects::nonNull)
				.filter(symbol -> "TRADING".equals(symbol.status())).filter(BinanceSymbol::isSpotTradingAllowed)
				.limit(200)
				.map(BinanceSymbol::symbol).filter(Objects::nonNull).distinct().sorted().toList();
	}

	private String toTradeStreamName(String symbol) {
		return "%s@trade".formatted(symbol.toLowerCase(Locale.ROOT));
	}

	private static <T> List<List<T>> partition(List<T> source, int batchSize) {
		if (source == null || source.isEmpty()) {
			return List.of();
		}

		if (batchSize <= 0) {
			throw new IllegalArgumentException("Batch size must be positive.");
		}

		var partitions = new ArrayList<List<T>>();

		for (int start = 0; start < source.size(); start += batchSize) {
			int end = Math.min(start + batchSize, source.size());
			partitions.add(source.subList(start, end));
		}

		return partitions;
	}

	public record BinanceExchangeInfo(List<BinanceSymbol> symbols) {
	}

	public record BinanceSymbol(String symbol, String status, boolean isSpotTradingAllowed) {
	}
}
