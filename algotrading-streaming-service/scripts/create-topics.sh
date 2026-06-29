#!/usr/bin/env bash
set -euo pipefail

KAFKA_HOME=${KAFKA_HOME:-/opt/kafka}
BOOTSTRAP=${BOOTSTRAP:-localhost:9092,localhost:9096,localhost:9097,localhost:9098,localhost:9099}
PARTITIONS=${PARTITIONS:-12}
REPLICATION_FACTOR=${REPLICATION_FACTOR:-3}

create_topic() {
  local topic=$1
  local retention_ms=${2:-86400000}
  "$KAFKA_HOME/bin/kafka-topics.sh" \
    --bootstrap-server "$BOOTSTRAP" \
    --create \
    --if-not-exists \
    --topic "$topic" \
    --partitions "$PARTITIONS" \
    --replication-factor "$REPLICATION_FACTOR" \
    --config "retention.ms=$retention_ms"
}

create_topic trades 86400000
create_topic market-analytics.trades.validated 86400000
create_topic market-analytics.candles-1m 604800000
create_topic market-analytics.vwap-5m 604800000
create_topic market-analytics.alerts 604800000
