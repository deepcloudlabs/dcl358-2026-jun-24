package com.example.algotrading.streaming.model;

public class VwapWindow {
    private String symbol;
    private long windowStart;
    private long windowEnd;
    private double tradedVolume;
    private double quoteVolume;
    private double minPrice;
    private double maxPrice;
    private long tradeCount;
    private long firstTradeTimestamp;
    private long lastTradeTimestamp;
    private boolean initialized;

    public VwapWindow add(String symbol, TradeEvent trade) {
        if (!initialized) {
            this.symbol = symbol;
            this.minPrice = trade.price();
            this.maxPrice = trade.price();
            this.firstTradeTimestamp = trade.timestamp();
            this.initialized = true;
        }

        this.symbol = symbol;
        this.tradedVolume += trade.quantity();
        this.quoteVolume += trade.price() * trade.quantity();
        this.minPrice = Math.min(this.minPrice, trade.price());
        this.maxPrice = Math.max(this.maxPrice, trade.price());
        this.tradeCount++;
        this.lastTradeTimestamp = trade.timestamp();
        return this;
    }

    public VwapWindow withWindow(long windowStart, long windowEnd) {
        var copy = new VwapWindow();
        copy.symbol = this.symbol;
        copy.windowStart = windowStart;
        copy.windowEnd = windowEnd;
        copy.tradedVolume = this.tradedVolume;
        copy.quoteVolume = this.quoteVolume;
        copy.minPrice = this.minPrice;
        copy.maxPrice = this.maxPrice;
        copy.tradeCount = this.tradeCount;
        copy.firstTradeTimestamp = this.firstTradeTimestamp;
        copy.lastTradeTimestamp = this.lastTradeTimestamp;
        copy.initialized = this.initialized;
        return copy;
    }

    public double getVwap() {
        if (tradedVolume == 0.0) {
            return 0.0;
        }
        return quoteVolume / tradedVolume;
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

    public double getTradedVolume() {
        return tradedVolume;
    }

    public void setTradedVolume(double tradedVolume) {
        this.tradedVolume = tradedVolume;
    }

    public double getQuoteVolume() {
        return quoteVolume;
    }

    public void setQuoteVolume(double quoteVolume) {
        this.quoteVolume = quoteVolume;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
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

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }
}
