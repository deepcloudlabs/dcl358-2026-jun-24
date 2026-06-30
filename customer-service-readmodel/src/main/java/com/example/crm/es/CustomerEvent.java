package com.example.crm.es;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Document(collection = "customer-es-events")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @Type(value = CustomerCreatedEvent.class, name = "CUSTOMER_CREATED"),
        @Type(value = CustomerFullnameChangedEvent.class, name = "CUSTOMER_FULLNAME_CHANGED"),
        @Type(value = CustomerRemovedEvent.class, name = "CUSTOMER_REMOVED"),
        @Type(value = CustomerAddressesChangedEvent.class, name = "CUSTOMER_ADDRESS_CHANGED"),
        @Type(value = CustomerPhonesChangedEvent.class, name = "CUSTOMER_PHONE_CHANGED")
})
public abstract class CustomerEvent {
    @Id
    private String eventId = UUID.randomUUID().toString();
    private String customerIdentity;
    private long sequenceNumber;
    private Instant occurredAt = Instant.now();

    protected CustomerEvent() {
    }

    protected CustomerEvent(String customerIdentity, long sequenceNumber) {
        this.customerIdentity = customerIdentity;
        this.sequenceNumber = sequenceNumber;
    }

    public abstract CustomerEventType eventType();

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCustomerIdentity() {
        return customerIdentity;
    }

    public void setCustomerIdentity(String customerIdentity) {
        this.customerIdentity = customerIdentity;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
