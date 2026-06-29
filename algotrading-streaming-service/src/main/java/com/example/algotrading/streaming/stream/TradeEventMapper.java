package com.example.algotrading.streaming.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.algotrading.streaming.model.TradeEvent;

import tools.jackson.databind.ObjectMapper;

@Component
public class TradeEventMapper {
    private static final Logger log = LoggerFactory.getLogger(TradeEventMapper.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TradeEvent fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, TradeEvent.class);
        } catch (Exception ex) {
            log.warn("Dropping malformed trade event: {}", json, ex);
            return null;
        }
    }
}
