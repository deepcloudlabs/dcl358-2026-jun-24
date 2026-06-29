# Algotrading Streaming Service

## Scenario

The existing `algotrading-service` acts as the market-ingestion boundary. It opens Binance WebSocket connections for several spot markets, receives trade events, normalizes every trade into a JSON document, and publishes it to the Kafka topic `trades`.

This service implements the analytical streaming boundary. In a realistic algorithmic-trading or market-surveillance setting, downstream components should not work directly on raw tick traffic. They need deterministic, queryable, and event-time based features such as OHLCV candles, VWAP windows, and volatility alerts. The goal is therefore to transform the raw trade stream into operational market-intelligence topics and local queryable state stores.

## Input contract inherited from producer

Topic: `trades`

Key: market symbol, for example `BTCUSDT`

Value:

```json
{
  "symbol": "BTCUSDT",
  "price": 65000.12,
  "quantity": 0.003,
  "timestamp": 1719850000000,
  "sequence": 123456789,
  "tradeId": 1719850000100
}
```

The producer code writes the JSON value as a `TradeDocument` and uses `tradeEvent.symbol()` as the Kafka key. This service is intentionally compatible with that contract.

## Solution design

The topology follows a stateful Kafka Streams architecture:

1. Consume raw JSON trade events from `trades`.
2. Parse and validate the payload.
3. Re-key records by upper-case symbol to guarantee symbol-local aggregation.
4. Produce validated trades to `market-analytics.trades.validated`.
5. Build one-minute OHLCV candles using event-time tumbling windows.
6. Build five-minute VWAP windows using event-time tumbling windows.
7. Detect intrawindow price-range expansion and publish alerts.
8. Materialize queryable local state stores for REST-based operational inspection.

## Topics

| Topic | Direction | Description |
|---|---:|---|
| `trades` | input | Raw normalized trade events from the producer service. |
| `market-analytics.trades.validated` | output | Parsed and schema-valid trade events. |
| `market-analytics.candles-1m` | output | One-minute OHLCV candles per symbol. |
| `market-analytics.vwap-5m` | output | Five-minute VWAP windows per symbol. |
| `market-analytics.alerts` | output | Volatility/range-expansion signals. |

## State stores

| Store | Type | Purpose |
|---|---|---|
| `ohlcv-1m-store` | window store | Query recent OHLCV candles by symbol. |
| `vwap-5m-store` | window store | Query recent VWAP windows by symbol. |

## Run order

Start Kafka first, then create the topics, then start the producer, and finally start this streaming service.

For your five-node local Kafka cluster, the default bootstrap configuration is:

```properties
localhost:9092,localhost:9096,localhost:9097,localhost:9098,localhost:9099
```

Create topics on Windows:

```cmd
set KAFKA_HOME=C:\DEVEL\stage\opt\kafka_2.13-4.3.0
scripts\create-topics.cmd
```

Create topics on Linux/macOS:

```bash
export KAFKA_HOME=/opt/kafka
./scripts/create-topics.sh
```

Build and run with Maven:

```bash
mvn clean package
mvn spring-boot:run
```

On Windows:

```cmd
mvn clean package
mvn spring-boot:run
```

## REST API

Health and topology state:

```http
GET http://localhost:5050/api/analytics/health
```

Recent one-minute candles:

```http
GET http://localhost:5050/api/analytics/markets/BTCUSDT/candles/recent?minutes=15
```

Recent five-minute VWAP windows:

```http
GET http://localhost:5050/api/analytics/markets/BTCUSDT/vwap/recent?minutes=30
```

## Consume output topics

Windows example:

```cmd
%KAFKA_HOME%\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic market-analytics.candles-1m --from-beginning
%KAFKA_HOME%\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic market-analytics.vwap-5m --from-beginning
%KAFKA_HOME%\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic market-analytics.alerts --from-beginning
```

## Important implementation notes

The topology uses the trade event timestamp field as event time through `TradeTimestampExtractor`. This is critical because Binance traffic can arrive with network jitter, and analytics windows should reflect market time rather than only ingestion time.

The application uses at-least-once processing by default. If the broker and topic configuration are later prepared for exactly-once workloads, set:

```properties
spring.kafka.streams.properties.processing.guarantee=exactly_once_v2
```

The default alert threshold is intentionally low for demonstration:

```properties
trading.analytics.price-range-alert-percentage=0.25
```

Increase this threshold in production to reduce alert noise.
