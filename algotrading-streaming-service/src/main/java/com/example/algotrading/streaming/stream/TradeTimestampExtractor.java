package com.example.algotrading.streaming.stream;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import com.example.algotrading.streaming.model.TradeEvent;

import tools.jackson.databind.ObjectMapper;

public class TradeTimestampExtractor implements TimestampExtractor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        try {
            if (record.value() instanceof String json) {
                var trade = objectMapper.readValue(json, TradeEvent.class);
                if (trade.timestamp() > 0L) {
                    return trade.timestamp();
                }
            }
        } catch (Exception ignored) {
            // Fallback intentionally uses the Kafka record timestamp.
        }

        if (record.timestamp() > 0L) {
            return record.timestamp();
        }
        if (partitionTime > 0L) {
            return partitionTime;
        }
        return System.currentTimeMillis();
    }
}
