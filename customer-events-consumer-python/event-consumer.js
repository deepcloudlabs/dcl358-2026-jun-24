const {Kafka, logLevel} = require("kafkajs");

const kafka = new Kafka({
    clientId: "customer-events-consumer",
    brokers: [
        "127.0.0.1:9092",
        "127.0.0.1:9096",
        "127.0.0.1:9097",
        "127.0.0.1:9098",
        "127.0.0.1:9099",
    ],
    logLevel: logLevel.INFO,
});

const consumer = kafka.consumer({
    groupId: "customer-events-consumer-group",
});

function parseMessageValue(message) {
    if (!message.value) {
        return null;
    }

    const rawValue = message.value.toString("utf8");

    try {
        return JSON.parse(rawValue);
    } catch {
        return rawValue;
    }
}

async function handleCustomerEvent({topic, partition, message}) {
    const key = message.key ? message.key.toString("utf8") : null;
    const value = parseMessageValue(message);

    const headers = {};
    if (message.headers) {
        for (const [headerKey, headerValue] of Object.entries(message.headers)) {
            headers[headerKey] = headerValue ? headerValue.toString("utf8") : null;
        }
    }

    console.log("Customer event received:");
    console.log({
        topic,
        partition,
        offset: message.offset,
        timestamp: message.timestamp,
        key,
        value,
        headers,
    });


}

async function start() {
    await consumer.connect();

    await consumer.subscribe({
        topic: "customer-events",
        fromBeginning: true,
    });

    await consumer.run({
        eachMessage: async ({topic, partition, message}) => {
            await handleCustomerEvent({topic, partition, message});
        },
    });

    console.log("Customer events consumer started.");
}

async function shutdown() {
    console.log("Shutting down customer events consumer...");

    try {
        await consumer.disconnect();
        console.log("Consumer disconnected.");
        process.exit(0);
    } catch (error) {
        console.error("Error while disconnecting consumer:", error);
        process.exit(1);
    }
}

process.on("SIGINT", shutdown);
process.on("SIGTERM", shutdown);

start().catch(async (error) => {
    console.error("Fatal consumer error:", error);

    try {
        await consumer.disconnect();
    } catch {
        // ignore disconnect errors during fatal shutdown
    }

    process.exit(1);
});