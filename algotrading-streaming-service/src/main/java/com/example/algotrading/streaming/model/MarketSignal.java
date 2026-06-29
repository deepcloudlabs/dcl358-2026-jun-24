package com.example.algotrading.streaming.model;

public record MarketSignal(
        String symbol,
        String signalType,
        String severity,
        String message,
        long windowStart,
        long windowEnd,
        double referencePrice,
        double observedValue,
        double thresholdPercentage,
        long createdAt) {

    public static MarketSignal rangeExpansion(Candle candle, double thresholdPercentage) {
        if (candle == null || !candle.isInitialized()) {
            return null;
        }

        var rangePercentage = candle.getPriceRangePercentage();
        if (rangePercentage < thresholdPercentage) {
            return null;
        }

        var severity = rangePercentage >= thresholdPercentage * 3.0 ? "HIGH" : "MEDIUM";
        var message = "Intrawindow price range exceeded configured threshold.";
        return new MarketSignal(
                candle.getSymbol(),
                "PRICE_RANGE_EXPANSION",
                severity,
                message,
                candle.getWindowStart(),
                candle.getWindowEnd(),
                candle.getOpen(),
                rangePercentage,
                thresholdPercentage,
                System.currentTimeMillis());
    }
}
