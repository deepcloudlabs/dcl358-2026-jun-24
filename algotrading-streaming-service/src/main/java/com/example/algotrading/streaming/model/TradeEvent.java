package com.example.algotrading.streaming.model;

public record TradeEvent(
        String symbol,
        double price,
        double quantity,
        long timestamp,
        long sequence,
        long tradeId) {

    public boolean isValid() {
        return symbol != null
                && !symbol.isBlank()
                && Double.isFinite(price)
                && Double.isFinite(quantity)
                && price > 0.0
                && quantity > 0.0
                && timestamp > 0L;
    }
}
