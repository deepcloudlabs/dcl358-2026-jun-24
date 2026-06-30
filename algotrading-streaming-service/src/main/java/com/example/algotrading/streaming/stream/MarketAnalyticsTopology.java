package com.example.algotrading.streaming.stream;

import java.util.Locale;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.WindowStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import com.example.algotrading.streaming.config.TradingAnalyticsProperties;
import com.example.algotrading.streaming.model.Candle;
import com.example.algotrading.streaming.model.MarketSignal;
import com.example.algotrading.streaming.model.TradeEvent;
import com.example.algotrading.streaming.model.VwapWindow;
import com.example.algotrading.streaming.serde.JsonSerde;

@Configuration
@EnableKafkaStreams
@EnableConfigurationProperties(TradingAnalyticsProperties.class)
public class MarketAnalyticsTopology {
    private static final Logger log = LoggerFactory.getLogger(MarketAnalyticsTopology.class);

    @Bean
    KStream<String, String> marketTopology(
            StreamsBuilder builder,
            TradingAnalyticsProperties properties,
            TradeEventMapper tradeEventMapper) {

        var stringSerde = Serdes.String();
        var tradeSerde = new JsonSerde<>(TradeEvent.class);
        var candleSerde = new JsonSerde<>(Candle.class);
        var vwapSerde = new JsonSerde<>(VwapWindow.class);
        var signalSerde = new JsonSerde<>(MarketSignal.class);

        KStream<String, String> rawTrades = builder.stream(
                properties.getInputTopic(),
                Consumed.with(stringSerde, stringSerde));

        KStream<String, TradeEvent> trades = rawTrades
                .mapValues(tradeEventMapper::fromJson)
                .filter((_, trade) -> trade != null && trade.isValid())
                .selectKey((_, trade) -> trade.symbol().toUpperCase(Locale.ROOT));

        trades.to(properties.getValidatedTradesTopic(), Produced.with(stringSerde, tradeSerde));
        // Hopping Window
        // 10:19-10:29
        // 10:29:10:39
        // Tumbling window
        // 10:19-10:29
        // 10:21-10:31
        
        KTable<Windowed<String>, Candle> oneMinuteCandles = trades
                .groupByKey(Grouped.with(stringSerde, tradeSerde))
                .windowedBy(TimeWindows.ofSizeAndGrace(properties.getCandleWindow(), properties.getWindowGrace()))
                .aggregate(
                        Candle::new,
                        (symbol, trade, candle) -> candle.add(symbol, trade),
                        Materialized.<String, Candle, WindowStore<Bytes, byte[]>>as(properties.getCandleStoreName())
                                .withKeySerde(stringSerde)
                                .withValueSerde(candleSerde));

        oneMinuteCandles
                .toStream()
                .map((windowedSymbol, candle) -> KeyValue.pair(
                        windowedSymbol.key(),
                        candle.withWindow(windowedSymbol.window().start(), windowedSymbol.window().end())))
                .peek((symbol, candle) -> log.info("1m candle {} {}", symbol, candle.getClose()))
                .to(properties.getCandleTopic(), Produced.with(stringSerde, candleSerde));

        oneMinuteCandles
                .toStream()
                .map((windowedSymbol, candle) -> {
                    var windowedCandle = candle.withWindow(windowedSymbol.window().start(), windowedSymbol.window().end());
                    return KeyValue.pair(
                            windowedSymbol.key(),
                            MarketSignal.rangeExpansion(windowedCandle, properties.getPriceRangeAlertPercentage()));
                })
                .filter((_, signal) -> signal != null)
                .to(properties.getAlertTopic(), Produced.with(stringSerde, signalSerde));

        KTable<Windowed<String>, VwapWindow> fiveMinuteVwap = trades
                .groupByKey(Grouped.with(stringSerde, tradeSerde))
                .windowedBy(TimeWindows.ofSizeAndGrace(properties.getVwapWindow(), properties.getWindowGrace()))
                .aggregate(
                        VwapWindow::new,
                        (symbol, trade, vwapWindow) -> vwapWindow.add(symbol, trade),
                        Materialized.<String, VwapWindow, WindowStore<Bytes, byte[]>>as(properties.getVwapStoreName())
                                .withKeySerde(stringSerde)
                                .withValueSerde(vwapSerde));

        fiveMinuteVwap
                .toStream()
                .map((windowedSymbol, vwapWindow) -> KeyValue.pair(
                        windowedSymbol.key(),
                        vwapWindow.withWindow(windowedSymbol.window().start(), windowedSymbol.window().end())))
                .peek((symbol, vwapWindow) -> log.info("5m VWAP {} {}", symbol, vwapWindow.getVwap()))
                .to(properties.getVwapTopic(), Produced.with(stringSerde, vwapSerde));

        return rawTrades;
    }
}
