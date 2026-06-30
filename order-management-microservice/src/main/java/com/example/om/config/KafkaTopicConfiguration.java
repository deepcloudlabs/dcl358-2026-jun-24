package com.example.om.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import com.example.saga.contract.SagaTopics;

@Configuration
public class KafkaTopicConfiguration {
    private static final int PARTITIONS = 3;
    private static final int REPLICAS = 1;

    @Bean
    NewTopic paymentCommandsTopic() {
        return TopicBuilder.name(SagaTopics.ORDER_PAYMENT_COMMAND).partitions(PARTITIONS).replicas(REPLICAS).build();
    }

    @Bean
    NewTopic paymentRepliesTopic() {
        return TopicBuilder.name(SagaTopics.ORDER_PAYMENT_REPLY).partitions(PARTITIONS).replicas(REPLICAS).build();
    }

    @Bean
    NewTopic inventoryCommandsTopic() {
        return TopicBuilder.name(SagaTopics.ORDER_INVENTORY_COMMAND).partitions(PARTITIONS).replicas(REPLICAS).build();
    }

    @Bean
    NewTopic inventoryRepliesTopic() {
        return TopicBuilder.name(SagaTopics.ORDER_INVENTORY_REPLY).partitions(PARTITIONS).replicas(REPLICAS).build();
    }

    @Bean
    NewTopic shippingCommandsTopic() {
        return TopicBuilder.name(SagaTopics.ORDER_SHIPPING_COMMAND).partitions(PARTITIONS).replicas(REPLICAS).build();
    }

    @Bean
    NewTopic shippingRepliesTopic() {
        return TopicBuilder.name(SagaTopics.ORDER_SHIPPING_REPLY).partitions(PARTITIONS).replicas(REPLICAS).build();
    }

    @Bean
    NewTopic orderEventsTopic() {
        return TopicBuilder.name(SagaTopics.ORDER_EVENTS).partitions(PARTITIONS).replicas(REPLICAS).build();
    }
}
