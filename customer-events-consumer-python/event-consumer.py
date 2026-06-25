import json

from kafka import KafkaConsumer

consumer = KafkaConsumer(
    "customer-events",
    bootstrap_servers=[
        'localhost:9096',
        'localhost:9097',
        'localhost:9098',
        'localhost:9099'
    ],
    group_id='dcl358-python',
    auto_offset_reset='earliest',
    enable_auto_commit=True,
    value_deserializer=lambda v: json.loads(v.decode('utf-8'))
)

for message in consumer:
    trade = json.loads(message.value)
    print(trade)