package com.example.crm.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Bean
    NewTopic customerEventsTopic(
    		@Value("${app.kafka.customer-events-topic:crm-es-events}") String topicName,
    		@Value("${app.kafka.customer-events-partitions:3}") int numOfPartitions) {
        return TopicBuilder.name(topicName)
                .partitions(numOfPartitions)
                .replicas(1)
                .build();
    }
}
