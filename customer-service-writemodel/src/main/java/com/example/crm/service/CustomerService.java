package com.example.crm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.crm.domain.CustomerAggregate;
import com.example.crm.dto.request.CreateCustomerRequest;
import com.example.crm.dto.request.UpdateCustomerRequest;
import com.example.crm.dto.response.CreateCustomerResponse;
import com.example.crm.dto.response.DeleteCustomerResponse;
import com.example.crm.dto.response.UpdateCustomerResponse;
import com.example.crm.es.CustomerEvent;
import com.example.crm.repository.CustomerEventSourceRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class CustomerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerEventSourceRepository eventSourceRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String customerEventsTopic;

    public CustomerService(CustomerEventSourceRepository eventSourceRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.customer-events-topic:crm-es-events}") String customerEventsTopic) {
        this.eventSourceRepository = eventSourceRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.customerEventsTopic = customerEventsTopic;
    }

    public CreateCustomerResponse acquireCustomer(CreateCustomerRequest request) {
        var aggregate = rehydrate(request.identity());
        var event = aggregate.create(request);
        var savedEvent = persistAndPublish(event);
        return new CreateCustomerResponse("accepted", savedEvent.getCustomerIdentity(), savedEvent.getSequenceNumber());
    }

    public UpdateCustomerResponse updateCustomer(String identity, UpdateCustomerRequest request) {
        var aggregate = rehydrate(identity);
        var events = aggregate.change(identity, request);
        var savedSequenceNumbers = events.stream()
                .map(this::persistAndPublish)
                .map(CustomerEvent::getSequenceNumber)
                .toList();
        return new UpdateCustomerResponse("accepted", identity, savedSequenceNumbers);
    }

    public DeleteCustomerResponse releaseCustomer(String identity) {
        var aggregate = rehydrate(identity);
        var event = aggregate.remove(identity);
        var savedEvent = persistAndPublish(event);
        return new DeleteCustomerResponse("accepted", savedEvent.getCustomerIdentity(), savedEvent.getSequenceNumber());
    }

    private CustomerAggregate rehydrate(String identity) {
        var history = eventSourceRepository.findByCustomerIdentityOrderBySequenceNumberAsc(identity);
        return CustomerAggregate.rehydrate(history);
    }

    private CustomerEvent persistAndPublish(CustomerEvent event) {
        var savedEvent = eventSourceRepository.insert(event);
        try {
            var eventAsJson = objectMapper.writeValueAsString(savedEvent);
            kafkaTemplate.send(customerEventsTopic, savedEvent.getCustomerIdentity(), eventAsJson).join();
            LOGGER.info("Published customer event: type={}, aggregate={}, sequence={}", savedEvent.eventType(),
                    savedEvent.getCustomerIdentity(), savedEvent.getSequenceNumber());
            return savedEvent;
        } catch (Exception exception) {
            LOGGER.error("Event was persisted but could not be published: eventId={}, aggregate={}, sequence={}",
                    savedEvent.getEventId(), savedEvent.getCustomerIdentity(), savedEvent.getSequenceNumber(), exception);
            throw new IllegalStateException("Event was persisted but could not be published to Kafka.", exception);
        }
    }
}
