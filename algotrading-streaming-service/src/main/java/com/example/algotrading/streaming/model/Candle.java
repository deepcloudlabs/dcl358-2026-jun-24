package com.example.algotrading.streaming.model;

public class Candle {
    private String symbol;
    private long windowStart;
    private long windowEnd;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private double quoteVolume;
    private long tradeCount;
    private long firstTradeTimestamp;
    private long lastTradeTimestamp;
    private long firstTradeId;
    private long lastTradeId;
    private boolean initialized;

    public Candle add(String symbol, TradeEvent trade) {
        if (!initialized) {
            this.symbol = symbol;
            this.open = trade.price();
            this.high = trade.price();
            this.low = trade.price();
            this.close = trade.price();
            this.firstTradeTimestamp = trade.timestamp();
            this.firstTradeId = trade.tradeId();
            this.initialized = true;
        }

        this.symbol = symbol;
        this.high = Math.max(this.high, trade.price());
        this.low = Math.min(this.low, trade.price());
        this.close = trade.price();
        this.volume += trade.quantity();
        this.quoteVolume += trade.price() * trade.quantity();
        this.tradeCount++;
        this.lastTradeTimestamp = trade.timestamp();
        this.lastTradeId = trade.tradeId();
        return this;
    }

    public Candle withWindow(long windowStart, long windowEnd) {
        var copy = new Candle();
        copy.symbol = this.symbol;
        copy.windowStart = windowStart;
        copy.windowEnd = windowEnd;
        copy.open = this.open;
        copy.high = this.high;
        copy.low = this.low;
        copy.close = this.close;
        copy.volume = this.volume;
        copy.quoteVolume = this.quoteVolume;
        copy.tradeCount = this.tradeCount;
        copy.firstTradeTimestamp = this.firstTradeTimestamp;
        copy.lastTradeTimestamp = this.lastTradeTimestamp;
        copy.firstTradeId = this.firstTradeId;
        copy.lastTradeId = this.lastTradeId;
        copy.initialized = this.initialized;
        return copy;
    }

    public double getVwap() {
        if (volume == 0.0) {
            return 0.0;
        }
        return quoteVolume / volume;
    }

    public double getPriceRangePercentage() {
        if (!initialized || open == 0.0) {
            return 0.0;
        }
        return ((high - low) / open) * 100.0;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getWindowStart() {
        return windowStart;
    }

    public void setWindowStart(long windowStart) {
        this.windowStart = windowStart;
    }

    public long getWindowEnd() {
        return windowEnd;
    }

    public void setWindowEnd(long windowEnd) {
        this.windowEnd = windowEnd;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getQuoteVolume() {
        return quoteVolume;
    }

    public void setQuoteVolume(double quoteVolume) {
        this.quoteVolume = quoteVolume;
    }

    public long getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(long tradeCount) {
        this.tradeCount = tradeCount;
    }

    public long getFirstTradeTimestamp() {
        return firstTradeTimestamp;
    }

    public void setFirstTradeTimestamp(long firstTradeTimestamp) {
        this.firstTradeTimestamp = firstTradeTimestamp;
    }

    public long getLastTradeTimestamp() {
        return lastTradeTimestamp;
    }

    public void setLastTradeTimestamp(long lastTradeTimestamp) {
        this.lastTradeTimestamp = lastTradeTimestamp;
    }

    public long getFirstTradeId() {
        return firstTradeId;
    }

    public void setFirstTradeId(long firstTradeId) {
        this.firstTradeId = firstTradeId;
    }

    public long getLastTradeId() {
        return lastTradeId;
    }

    public void setLastTradeId(long lastTradeId) {
        this.lastTradeId = lastTradeId;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
