package com.example.shipping.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.saga.contract.SagaTopics;
import com.example.saga.contract.ShippingCommand;
import com.example.saga.contract.ShippingCommandType;
import com.example.saga.contract.ShippingReply;
import com.example.saga.contract.ShippingStatus;

import tools.jackson.databind.ObjectMapper;

@Service
public class ShippingService {
    private static final Logger logger = LoggerFactory.getLogger(ShippingService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final Map<Long, String> shipmentsByOrder = new ConcurrentHashMap<>();
    private final Map<UUID, ShippingReply> processedCommands = new ConcurrentHashMap<>();

    public ShippingService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = SagaTopics.ORDER_SHIPPING_COMMAND, groupId = "${spring.kafka.consumer.group-id}")
    public void handleShippingCommand(String payload) {
        var command = read(payload);
        var reply = processedCommands.computeIfAbsent(command.messageId(), _ -> execute(command));
        send(reply);
    }

    private ShippingReply execute(ShippingCommand command) {
        if (command.commandType() == ShippingCommandType.CANCEL_SHIPMENT) {
            shipmentsByOrder.remove(command.orderId());
            logger.info("Canceled shipment for order {}.", command.orderId());
            return new ShippingReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                    ShippingStatus.CANCELED, "Shipment canceled.");
        }

        if (command.customerId().toUpperCase().startsWith("NOSHIP")) {
            logger.info("Rejecting shipment for order {} because customer is marked as non-shippable.", command.orderId());
            return new ShippingReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                    ShippingStatus.REJECTED, "Customer address is not shippable.");
        }

        var shipmentNumber = "SHP-%d".formatted(command.orderId());
        shipmentsByOrder.put(command.orderId(), shipmentNumber);
        logger.info("Created shipment {} for order {}.", shipmentNumber, command.orderId());
        return new ShippingReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                ShippingStatus.CREATED, "Shipment %s created.".formatted(shipmentNumber));
    }

    private ShippingCommand read(String payload) {
        try {
            return objectMapper.readValue(payload, ShippingCommand.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to deserialize shipping command: %s".formatted(payload), ex);
        }
    }

    private void send(ShippingReply reply) {
        try {
            kafkaTemplate.send(SagaTopics.ORDER_SHIPPING_REPLY, Long.toString(reply.orderId()), objectMapper.writeValueAsString(reply));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to publish shipping reply.", ex);
        }
    }
}
