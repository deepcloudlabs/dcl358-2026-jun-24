package com.example.om.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.om.controller.CreateOrderRequest;
import com.example.om.domain.Order;
import com.example.om.domain.OrderItem;
import com.example.om.domain.OrderStatus;
import com.example.om.repository.OrderRepository;
import com.example.saga.contract.InventoryCommand;
import com.example.saga.contract.InventoryCommandType;
import com.example.saga.contract.InventoryReply;
import com.example.saga.contract.InventoryStatus;
import com.example.saga.contract.OrderItemMessage;
import com.example.saga.contract.OrderLifecycleEvent;
import com.example.saga.contract.OrderLifecycleEventType;
import com.example.saga.contract.PaymentCommand;
import com.example.saga.contract.PaymentCommandType;
import com.example.saga.contract.PaymentReply;
import com.example.saga.contract.PaymentStatus;
import com.example.saga.contract.SagaTopics;
import com.example.saga.contract.ShippingCommand;
import com.example.saga.contract.ShippingCommandType;
import com.example.saga.contract.ShippingReply;
import com.example.saga.contract.ShippingStatus;

import tools.jackson.databind.ObjectMapper;

@Service
public class OrderSagaCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(OrderSagaCoordinator.class);

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OrderSagaCoordinator(OrderRepository orderRepository,
                                KafkaTemplate<String, String> kafkaTemplate,
                                ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        var items = request.items().stream()
                .map(item -> new OrderItem(item.sku(), item.unitPrice(), item.quantity()))
                .toList();
        var order = Order.create(request.customerId(), items);
        var savedOrder = orderRepository.saveAndFlush(order);

        savedOrder.mark(OrderStatus.PENDING_PAYMENT);
        publishPaymentCommand(savedOrder, PaymentCommandType.AUTHORIZE);
        publishLifecycleEvent(savedOrder, OrderLifecycleEventType.ORDER_CREATED, "Saga started; waiting for payment authorization.");
        return savedOrder;
    }

    @Transactional
    @KafkaListener(topics = SagaTopics.ORDER_PAYMENT_REPLY, groupId = "${spring.kafka.consumer.group-id}")
    public void handlePaymentReply(String payload) {
        var reply = read(payload, PaymentReply.class);
        orderRepository.findById(reply.orderId()).ifPresentOrElse(order -> processPaymentReply(order, reply),
                () -> logger.warn("Payment reply ignored because order {} does not exist.", reply.orderId()));
    }

    private void processPaymentReply(Order order, PaymentReply reply) {
        if (reply.status() == PaymentStatus.AUTHORIZED && order.is(OrderStatus.PENDING_PAYMENT)) {
            order.mark(OrderStatus.PENDING_INVENTORY);
            publishInventoryCommand(order, InventoryCommandType.RESERVE);
            publishLifecycleEvent(order, OrderLifecycleEventType.PAYMENT_AUTHORIZED, "Payment authorized.");
            return;
        }

        if (reply.status() == PaymentStatus.REJECTED && order.is(OrderStatus.PENDING_PAYMENT)) {
            order.mark(OrderStatus.PAYMENT_REJECTED);
            order.mark(OrderStatus.CANCELED);
            publishLifecycleEvent(order, OrderLifecycleEventType.ORDER_CANCELED, "Payment rejected: %s".formatted(reply.reason()));
            return;
        }

        if (reply.status() == PaymentStatus.REFUNDED && order.is(OrderStatus.COMPENSATING_PAYMENT)) {
            order.mark(OrderStatus.CANCELED);
            publishLifecycleEvent(order, OrderLifecycleEventType.COMPENSATION_COMPLETED, "Payment refund completed.");
            publishLifecycleEvent(order, OrderLifecycleEventType.ORDER_CANCELED, "Order canceled after compensation.");
            return;
        }

        logger.info("Ignoring idempotent or out-of-order payment reply for order {} in state {}: {}",
                order.getOrderId(), order.getStatus(), reply.status());
    }

    @Transactional
    @KafkaListener(topics = SagaTopics.ORDER_INVENTORY_REPLY, groupId = "${spring.kafka.consumer.group-id}")
    public void handleInventoryReply(String payload) {
        var reply = read(payload, InventoryReply.class);
        orderRepository.findById(reply.orderId()).ifPresentOrElse(order -> processInventoryReply(order, reply),
                () -> logger.warn("Inventory reply ignored because order {} does not exist.", reply.orderId()));
    }

    private void processInventoryReply(Order order, InventoryReply reply) {
        if (reply.status() == InventoryStatus.RESERVED && order.is(OrderStatus.PENDING_INVENTORY)) {
            order.mark(OrderStatus.PENDING_SHIPPING);
            publishShippingCommand(order, ShippingCommandType.CREATE_SHIPMENT);
            publishLifecycleEvent(order, OrderLifecycleEventType.INVENTORY_RESERVED, "Inventory reserved.");
            return;
        }

        if (reply.status() == InventoryStatus.REJECTED && order.is(OrderStatus.PENDING_INVENTORY)) {
            order.mark(OrderStatus.INVENTORY_REJECTED);
            order.mark(OrderStatus.COMPENSATING_PAYMENT);
            publishPaymentCommand(order, PaymentCommandType.REFUND);
            publishLifecycleEvent(order, OrderLifecycleEventType.COMPENSATION_STARTED,
                    "Inventory rejected; refunding payment. Reason: %s".formatted(reply.reason()));
            return;
        }

        if (reply.status() == InventoryStatus.RELEASED && order.is(OrderStatus.COMPENSATING_INVENTORY)) {
            order.mark(OrderStatus.COMPENSATING_PAYMENT);
            publishPaymentCommand(order, PaymentCommandType.REFUND);
            publishLifecycleEvent(order, OrderLifecycleEventType.COMPENSATION_STARTED,
                    "Inventory released; refunding payment.");
            return;
        }

        logger.info("Ignoring idempotent or out-of-order inventory reply for order {} in state {}: {}",
                order.getOrderId(), order.getStatus(), reply.status());
    }

    @Transactional
    @KafkaListener(topics = SagaTopics.ORDER_SHIPPING_REPLY, groupId = "${spring.kafka.consumer.group-id}")
    public void handleShippingReply(String payload) {
        var reply = read(payload, ShippingReply.class);
        orderRepository.findById(reply.orderId()).ifPresentOrElse(order -> processShippingReply(order, reply),
                () -> logger.warn("Shipping reply ignored because order {} does not exist.", reply.orderId()));
    }

    private void processShippingReply(Order order, ShippingReply reply) {
        if (reply.status() == ShippingStatus.CREATED && order.is(OrderStatus.PENDING_SHIPPING)) {
            order.mark(OrderStatus.COMPLETED);
            publishLifecycleEvent(order, OrderLifecycleEventType.SHIPMENT_CREATED, "Shipment created.");
            publishLifecycleEvent(order, OrderLifecycleEventType.ORDER_COMPLETED, "Saga completed successfully.");
            return;
        }

        if (reply.status() == ShippingStatus.REJECTED && order.is(OrderStatus.PENDING_SHIPPING)) {
            order.mark(OrderStatus.SHIPMENT_REJECTED);
            order.mark(OrderStatus.COMPENSATING_INVENTORY);
            publishInventoryCommand(order, InventoryCommandType.RELEASE);
            publishLifecycleEvent(order, OrderLifecycleEventType.COMPENSATION_STARTED,
                    "Shipment rejected; releasing inventory. Reason: %s".formatted(reply.reason()));
            return;
        }

        if (reply.status() == ShippingStatus.CANCELED && order.is(OrderStatus.COMPENSATING_INVENTORY)) {
            publishInventoryCommand(order, InventoryCommandType.RELEASE);
            publishLifecycleEvent(order, OrderLifecycleEventType.COMPENSATION_STARTED,
                    "Shipment canceled; releasing inventory.");
            return;
        }

        logger.info("Ignoring idempotent or out-of-order shipping reply for order {} in state {}: {}",
                order.getOrderId(), order.getStatus(), reply.status());
    }

    private void publishPaymentCommand(Order order, PaymentCommandType type) {
        var command = new PaymentCommand(UUID.randomUUID(), order.getSagaIdAsUuid(), order.getOrderId(),
                order.getCustomerId(), order.getTotal(), type);
        send(SagaTopics.ORDER_PAYMENT_COMMAND, order.getOrderId(), command);
    }

    private void publishInventoryCommand(Order order, InventoryCommandType type) {
        var command = new InventoryCommand(UUID.randomUUID(), order.getSagaIdAsUuid(), order.getOrderId(),
                toMessages(order), type);
        send(SagaTopics.ORDER_INVENTORY_COMMAND, order.getOrderId(), command);
    }

    private void publishShippingCommand(Order order, ShippingCommandType type) {
        var command = new ShippingCommand(UUID.randomUUID(), order.getSagaIdAsUuid(), order.getOrderId(),
                order.getCustomerId(), toMessages(order), type);
        send(SagaTopics.ORDER_SHIPPING_COMMAND, order.getOrderId(), command);
    }

    private void publishLifecycleEvent(Order order, OrderLifecycleEventType type, String detail) {
        var event = new OrderLifecycleEvent(UUID.randomUUID(), order.getSagaIdAsUuid(), order.getOrderId(),
                type, order.getStatus().name(), detail, Instant.now());
        send(SagaTopics.ORDER_EVENTS, order.getOrderId(), event);
    }

    private List<OrderItemMessage> toMessages(Order order) {
        return order.getItems().stream()
                .map(item -> new OrderItemMessage(item.getSku(), item.getUnitPrice(), item.getQuantity()))
                .toList();
    }

    private <T> T read(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to deserialize %s: %s".formatted(type.getSimpleName(), payload), ex);
        }
    }

    private void send(String topic, long orderId, Object message) {
        try {
            kafkaTemplate.send(topic, Long.toString(orderId), objectMapper.writeValueAsString(message));
        } catch (Exception ex) {
            throw new MessagePublicationException("Unable to publish message to topic %s.".formatted(topic), ex);
        }
    }
}
