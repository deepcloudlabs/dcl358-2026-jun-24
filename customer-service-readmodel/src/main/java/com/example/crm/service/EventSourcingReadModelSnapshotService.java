package com.example.crm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.crm.document.CustomerReadModel;
import com.example.crm.es.CustomerAddressesChangedEvent;
import com.example.crm.es.CustomerCreatedEvent;
import com.example.crm.es.CustomerEvent;
import com.example.crm.es.CustomerFullnameChangedEvent;
import com.example.crm.es.CustomerPhonesChangedEvent;
import com.example.crm.es.CustomerRemovedEvent;
import com.example.crm.repository.CustomerReadModelRepository;

import tools.jackson.databind.ObjectMapper;

@Service
public class EventSourcingReadModelSnapshotService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventSourcingReadModelSnapshotService.class);

    private final CustomerReadModelRepository customerReadModelRepository;
    private final ObjectMapper objectMapper;

    public EventSourcingReadModelSnapshotService(CustomerReadModelRepository customerReadModelRepository,
            ObjectMapper objectMapper) {
        this.customerReadModelRepository = customerReadModelRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.customer-events-topic:crm-es-events}", groupId = "${spring.kafka.consumer.group-id:customer-service-readmodel}")
    public void listenEventSourcing(String eventAsJson) {
        try {
            CustomerEvent event = objectMapper.readValue(eventAsJson, CustomerEvent.class);
            apply(event);
        } catch (Exception exception) {
            LOGGER.error("Could not deserialize or apply customer event: {}", eventAsJson, exception);
            throw new IllegalStateException("Could not deserialize or apply customer event.", exception);
        }
    }

    private void apply(CustomerEvent event) {
        switch (event) {
            case CustomerCreatedEvent created -> applyCreated(created);
            case CustomerFullnameChangedEvent changed -> applyFullnameChanged(changed);
            case CustomerAddressesChangedEvent changed -> applyAddressesChanged(changed);
            case CustomerPhonesChangedEvent changed -> applyPhonesChanged(changed);
            case CustomerRemovedEvent removed -> applyRemoved(removed);
            default -> throw new IllegalArgumentException("Unsupported event: " + event.getClass().getName());
        }
        LOGGER.info("Applied projection event: type={}, aggregate={}, sequence={}", event.eventType(),
                event.getCustomerIdentity(), event.getSequenceNumber());
    }

    private void applyCreated(CustomerCreatedEvent event) {
        var existing = customerReadModelRepository.findById(event.getCustomerIdentity());
        if (existing.isPresent() && existing.get().alreadyApplied(event.getSequenceNumber())) {
            LOGGER.debug("Skipping duplicate create event: aggregate={}, sequence={}", event.getCustomerIdentity(),
                    event.getSequenceNumber());
            return;
        }
        var customer = new CustomerReadModel(event.getCustomerIdentity(), event.getFullname(), event.getAddresses(),
                event.getPhones(), event.getSequenceNumber(), event.getOccurredAt());
        customerReadModelRepository.save(customer);
    }

    private void applyFullnameChanged(CustomerFullnameChangedEvent event) {
        updateExisting(event, customer -> customer.setFullname(event.getFullname()));
    }

    private void applyAddressesChanged(CustomerAddressesChangedEvent event) {
        updateExisting(event, customer -> customer.setAddresses(event.getAddresses()));
    }

    private void applyPhonesChanged(CustomerPhonesChangedEvent event) {
        updateExisting(event, customer -> customer.setPhones(event.getPhones()));
    }

    private void applyRemoved(CustomerRemovedEvent event) {
        customerReadModelRepository.deleteById(event.getCustomerIdentity());
    }

    private void updateExisting(CustomerEvent event, java.util.function.Consumer<CustomerReadModel> mutation) {
        customerReadModelRepository.findById(event.getCustomerIdentity()).ifPresentOrElse(customer -> {
            if (customer.alreadyApplied(event.getSequenceNumber())) {
                LOGGER.debug("Skipping duplicate event: aggregate={}, sequence={}", event.getCustomerIdentity(),
                        event.getSequenceNumber());
                return;
            }
            mutation.accept(customer);
            customer.markApplied(event.getSequenceNumber(), event.getOccurredAt());
            customerReadModelRepository.save(customer);
        }, () -> LOGGER.warn("Projection missing for event: aggregate={}, sequence={}, type={}",
                event.getCustomerIdentity(), event.getSequenceNumber(), event.eventType()));
    }
}
