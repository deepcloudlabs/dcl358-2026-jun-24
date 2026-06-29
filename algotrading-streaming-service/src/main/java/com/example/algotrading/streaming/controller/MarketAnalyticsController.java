package com.example.algotrading.streaming.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.algotrading.streaming.config.TradingAnalyticsProperties;
import com.example.algotrading.streaming.model.Candle;
import com.example.algotrading.streaming.model.VwapWindow;

@RestController
@RequestMapping("/api/analytics")
public class MarketAnalyticsController {
	private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
	private final TradingAnalyticsProperties properties;

	public MarketAnalyticsController(StreamsBuilderFactoryBean streamsBuilderFactoryBean,
			TradingAnalyticsProperties properties) {
		this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
		this.properties = properties;
	}

	@GetMapping("/health")
	public StreamHealth health() {
		var kafkaStreams = kafkaStreams();
		return new StreamHealth(kafkaStreams.state().name(), properties.getInputTopic());
	}

	@GetMapping("/markets/{symbol}/candles/recent")
	public List<Candle> recentCandles(@PathVariable String symbol, @RequestParam(defaultValue = "15") long minutes) {
		return fetchRecentWindowValues(properties.getCandleStoreName(), normalizeSymbol(symbol),
				Duration.ofMinutes(minutes), Candle.class);
	}

	@GetMapping("/markets/{symbol}/vwap/recent")
	public List<VwapWindow> recentVwap(@PathVariable String symbol, @RequestParam(defaultValue = "30") long minutes) {
		return fetchRecentWindowValues(properties.getVwapStoreName(), normalizeSymbol(symbol),
				Duration.ofMinutes(minutes), VwapWindow.class);
	}

	private <T> List<T> fetchRecentWindowValues(String storeName, String symbol, Duration lookback,
			Class<T> ignoredType) {
		try {
			ReadOnlyWindowStore<String, T> store = kafkaStreams()
					.store(StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.windowStore()));

			var to = Instant.now();
			var from = to.minus(lookback);
			var result = new ArrayList<T>();
			try (WindowStoreIterator<T> iterator = store.fetch(symbol, from, to)) {
				while (iterator.hasNext()) {
					result.add(iterator.next().value);
				}
			}
			return result;
		} catch (InvalidStateStoreException ex) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Kafka Streams state store is not queryable yet. Retry after the topology reaches RUNNING state.",
					ex);
		}
	}

	private KafkaStreams kafkaStreams() {
		var kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
		if (kafkaStreams == null) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Kafka Streams has not started yet.");
		}
		return kafkaStreams;
	}

	private static String normalizeSymbol(String symbol) {
		if (symbol == null || symbol.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Symbol must not be blank.");
		}
		return symbol.toUpperCase(Locale.ROOT);
	}

	public record StreamHealth(String state, String inputTopic) {
	}
}
