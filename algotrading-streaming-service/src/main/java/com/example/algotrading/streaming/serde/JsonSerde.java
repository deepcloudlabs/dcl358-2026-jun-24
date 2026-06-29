package com.example.algotrading.streaming.serde;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import tools.jackson.databind.ObjectMapper;

public final class JsonSerde<T> implements Serde<T> {
    private final JsonSerializer<T> serializer;
    private final JsonDeserializer<T> deserializer;

    public JsonSerde(Class<T> targetType) {
        var objectMapper = new ObjectMapper();
        this.serializer = new JsonSerializer<>(objectMapper);
        this.deserializer = new JsonDeserializer<>(objectMapper, targetType);
    }

    @Override
    public Serializer<T> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<T> deserializer() {
        return deserializer;
    }

    private static final class JsonSerializer<T> implements Serializer<T> {
        private final ObjectMapper objectMapper;

        private JsonSerializer(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public byte[] serialize(String topic, T data) {
            if (data == null) {
                return null;
            }
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception ex) {
                throw new SerializationException("Cannot serialize JSON value for topic " + topic, ex);
            }
        }
    }

    private static final class JsonDeserializer<T> implements Deserializer<T> {
        private final ObjectMapper objectMapper;
        private final Class<T> targetType;

        private JsonDeserializer(ObjectMapper objectMapper, Class<T> targetType) {
            this.objectMapper = objectMapper;
            this.targetType = targetType;
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            if (data == null || data.length == 0) {
                return null;
            }
            try {
                return objectMapper.readValue(data, targetType);
            } catch (Exception ex) {
                throw new SerializationException("Cannot deserialize JSON value from topic " + topic, ex);
            }
        }
    }
}
