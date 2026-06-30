package com.example.payment.service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.saga.contract.PaymentCommand;
import com.example.saga.contract.PaymentCommandType;
import com.example.saga.contract.PaymentReply;
import com.example.saga.contract.PaymentStatus;
import com.example.saga.contract.SagaTopics;

import tools.jackson.databind.ObjectMapper;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final BigDecimal authorizationLimit;
    private final Map<UUID, PaymentReply> processedCommands = new ConcurrentHashMap<>();

    public PaymentService(KafkaTemplate<String, String> kafkaTemplate,
                          ObjectMapper objectMapper,
                          @Value("${payment.authorization-limit:10000.00}") BigDecimal authorizationLimit) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.authorizationLimit = authorizationLimit;
    }

    @KafkaListener(topics = SagaTopics.ORDER_PAYMENT_COMMAND, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentCommand(String payload) {
        var command = read(payload, PaymentCommand.class);
        var reply = processedCommands.computeIfAbsent(command.messageId(), _ -> execute(command));
        send(reply);
    }

    private PaymentReply execute(PaymentCommand command) {
        if (command.commandType() == PaymentCommandType.REFUND) {
            logger.info("Refunding payment for order {}.", command.orderId());
            return new PaymentReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                    PaymentStatus.REFUNDED, "Payment refunded.");
        }

        if (command.amount().compareTo(authorizationLimit) > 0) {
            logger.info("Rejecting payment for order {} because {} exceeds limit {}.",
                    command.orderId(), command.amount(), authorizationLimit);
            return new PaymentReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                    PaymentStatus.REJECTED, "Authorization limit exceeded.");
        }

        logger.info("Authorizing payment for order {}.", command.orderId());
        return new PaymentReply(UUID.randomUUID(), command.messageId(), command.sagaId(), command.orderId(),
                PaymentStatus.AUTHORIZED, "Payment authorized.");
    }

    private PaymentCommand read(String payload, Class<PaymentCommand> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to deserialize payment command: %s".formatted(payload), ex);
        }
    }

    private void send(PaymentReply reply) {
        try {
            kafkaTemplate.send(SagaTopics.ORDER_PAYMENT_REPLY, Long.toString(reply.orderId()), objectMapper.writeValueAsString(reply));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to publish payment reply.", ex);
        }
    }
}
