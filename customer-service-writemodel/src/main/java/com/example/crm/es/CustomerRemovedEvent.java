package com.example.crm.es;

public class CustomerRemovedEvent extends CustomerEvent {
    public CustomerRemovedEvent() {
    }

    public CustomerRemovedEvent(String customerIdentity, long sequenceNumber) {
        super(customerIdentity, sequenceNumber);
    }

    @Override
    public CustomerEventType eventType() {
        return CustomerEventType.CUSTOMER_REMOVED;
    }
}
