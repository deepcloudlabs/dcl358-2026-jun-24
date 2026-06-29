package com.example.algotrading.streaming.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "trading.analytics")
public class TradingAnalyticsProperties {
    private String inputTopic = "trades";
    private String validatedTradesTopic = "market-analytics.trades.validated";
    private String candleTopic = "market-analytics.candles-1m";
    private String vwapTopic = "market-analytics.vwap-5m";
    private String alertTopic = "market-analytics.alerts";
    private String candleStoreName = "ohlcv-1m-store";
    private String vwapStoreName = "vwap-5m-store";
    private Duration candleWindow = Duration.ofMinutes(1);
    private Duration vwapWindow = Duration.ofMinutes(5);
    private Duration windowGrace = Duration.ofSeconds(10);

    /**
     * Percentage threshold used for intrawindow volatility alerts.
     * Example: 0.25 means 0.25% price range inside the candle window.
     */
    private double priceRangeAlertPercentage = 0.25;

    public String getInputTopic() {
        return inputTopic;
    }

    public void setInputTopic(String inputTopic) {
        this.inputTopic = inputTopic;
    }

    public String getValidatedTradesTopic() {
        return validatedTradesTopic;
    }

    public void setValidatedTradesTopic(String validatedTradesTopic) {
        this.validatedTradesTopic = validatedTradesTopic;
    }

    public String getCandleTopic() {
        return candleTopic;
    }

    public void setCandleTopic(String candleTopic) {
        this.candleTopic = candleTopic;
    }

    public String getVwapTopic() {
        return vwapTopic;
    }

    public void setVwapTopic(String vwapTopic) {
        this.vwapTopic = vwapTopic;
    }

    public String getAlertTopic() {
        return alertTopic;
    }

    public void setAlertTopic(String alertTopic) {
        this.alertTopic = alertTopic;
    }

    public String getCandleStoreName() {
        return candleStoreName;
    }

    public void setCandleStoreName(String candleStoreName) {
        this.candleStoreName = candleStoreName;
    }

    public String getVwapStoreName() {
        return vwapStoreName;
    }

    public void setVwapStoreName(String vwapStoreName) {
        this.vwapStoreName = vwapStoreName;
    }

    public Duration getCandleWindow() {
        return candleWindow;
    }

    public void setCandleWindow(Duration candleWindow) {
        this.candleWindow = candleWindow;
    }

    public Duration getVwapWindow() {
        return vwapWindow;
    }

    public void setVwapWindow(Duration vwapWindow) {
        this.vwapWindow = vwapWindow;
    }

    public Duration getWindowGrace() {
        return windowGrace;
    }

    public void setWindowGrace(Duration windowGrace) {
        this.windowGrace = windowGrace;
    }

    public double getPriceRangeAlertPercentage() {
        return priceRangeAlertPercentage;
    }

    public void setPriceRangeAlertPercentage(double priceRangeAlertPercentage) {
        this.priceRangeAlertPercentage = priceRangeAlertPercentage;
    }
}
