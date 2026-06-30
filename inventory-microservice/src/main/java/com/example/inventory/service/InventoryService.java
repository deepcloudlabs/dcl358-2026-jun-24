package com.example.inventory.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.saga.contract.InventoryCommand;
import com.example.saga.contract.InventoryCommandType;
import com.example.saga.contract.InventoryReply;
import com.example.saga.contract.InventoryStatus;
import com.example.saga.contract.OrderItemMessage;
import com.example.saga.contract.SagaTopics;

import tools.jackson.databind.ObjectMapper;

@Service
public class InventoryService {
    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final int maxItemQuantity;
    private final Map<Long, List<OrderItemMessage>> reservationsByOrder = new ConcurrentHashMap<>();
    private final Map<UUID, InventoryReply> processedCommands = new ConcurrentHashMap<>();

    public InventoryService(KafkaTemplate<String, String> kafkaTemplate,
                            ObjectMapper objectMapper,
                            @Value("${inventory.max-item-quantity:50}") int maxItemQuantity) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.maxItemQuantity = maxItemQuantity;
    }

    @KafkaListener(topics = SagaTopics.ORDER_INVENTORY_COMMAND, groupId = "${spring.kafka.consumer.group-id}")
    public void handleInventoryCommand(String payload) {
        var command = read(payload);
        var reply = processedCommands.computeIfAbsent(command.messageId(), ignored -> execute(command));
        send(reply);
    }

    private InventoryReply execute(InventoryCommand command) {
        if (command.commandType() == InventoryCommandType.RELEASE) {
            reservationsByOrder.remove(command.orderId());
            logger.info("Released inventory reservation for order {}.", command.orderId());
            return new InventoryReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                    InventoryStatus.RELEASED, "Inventory reservation released.");
        }

        if (reservationsByOrder.containsKey(command.orderId())) {
            logger.info("Inventory was already reserved for order {}.", command.orderId());
            return new InventoryReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                    InventoryStatus.RESERVED, "Inventory was already reserved.");
        }

        var unavailableItem = command.items().stream()
                .filter(item -> item.quantity() > maxItemQuantity || item.sku().toUpperCase().startsWith("OOS"))
                .findFirst();

        if (unavailableItem.isPresent()) {
            var item = unavailableItem.get();
            logger.info("Inventory rejected order {} because item {} is unavailable.", command.orderId(), item.sku());
            return new InventoryReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                    InventoryStatus.REJECTED, "Item %s is not available in requested quantity.".formatted(item.sku()));
        }

        reservationsByOrder.put(command.orderId(), command.items());
        logger.info("Reserved inventory for order {}.", command.orderId());
        return new InventoryReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                InventoryStatus.RESERVED, "Inventory reserved.");
    }

    private InventoryCommand read(String payload) {
        try {
            return objectMapper.readValue(payload, InventoryCommand.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to deserialize inventory command: %s".formatted(payload), ex);
        }
    }

    private void send(InventoryReply reply) {
        try {
            kafkaTemplate.send(SagaTopics.ORDER_INVENTORY_REPLY, Long.toString(reply.orderId()), objectMapper.writeValueAsString(reply));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to publish inventory reply.", ex);
        }
    }
}
