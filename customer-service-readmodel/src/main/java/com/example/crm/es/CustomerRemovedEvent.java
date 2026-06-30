package com.example.crm.es;

public class CustomerRemovedEvent extends CustomerEvent {
    public CustomerRemovedEvent() {
    }

    @Override
    public CustomerEventType eventType() {
        return CustomerEventType.CUSTOMER_REMOVED;
    }
}
