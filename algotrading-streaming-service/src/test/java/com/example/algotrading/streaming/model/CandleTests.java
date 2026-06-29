package com.example.algotrading.streaming.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CandleTests {

    @Test
    void shouldAggregateOhlcvValues() {
        var candle = new Candle();

        candle.add("BTCUSDT", new TradeEvent("BTCUSDT", 100.0, 2.0, 1_000L, 1L, 10L));
        candle.add("BTCUSDT", new TradeEvent("BTCUSDT", 120.0, 1.0, 2_000L, 2L, 11L));
        candle.add("BTCUSDT", new TradeEvent("BTCUSDT", 90.0, 3.0, 3_000L, 3L, 12L));

        assertThat(candle.getOpen()).isEqualTo(100.0);
        assertThat(candle.getHigh()).isEqualTo(120.0);
        assertThat(candle.getLow()).isEqualTo(90.0);
        assertThat(candle.getClose()).isEqualTo(90.0);
        assertThat(candle.getVolume()).isEqualTo(6.0);
        assertThat(candle.getTradeCount()).isEqualTo(3);
        assertThat(candle.getVwap()).isEqualTo((100.0 * 2.0 + 120.0 * 1.0 + 90.0 * 3.0) / 6.0);
    }
}
