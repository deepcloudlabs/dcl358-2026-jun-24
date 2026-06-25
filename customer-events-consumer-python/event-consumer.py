from confluent_kafka import Consumer, KafkaException
import json

BOOTSTRAP_SERVERS = "localhost:9092,localhost:9096,localhost:9097,localhost:9098,localhost:9099"
TOPIC = "customer-events"

consumer = Consumer({
    "bootstrap.servers": BOOTSTRAP_SERVERS,
    "group.id": "python-consumer-group",
    "client.id": "python-consumer",
    "auto.offset.reset": "earliest",
    "enable.auto.commit": True
})

consumer.subscribe([TOPIC])

try:
    while True:
        msg = consumer.poll(1.0)

        if msg is None:
            continue

        if msg.error():
            raise KafkaException(msg.error())

        key = msg.key().decode("utf-8") if msg.key() else None
        value = json.loads(msg.value().decode("utf-8"))

        print(
            f"Consumed key={key}, value={value}, "
            f"partition={msg.partition()}, offset={msg.offset()}"
        )

except KeyboardInterrupt:
    print("Consumer stopped.")

finally:
    consumer.close()