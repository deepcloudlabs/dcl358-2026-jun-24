package com.example.crm.es;

import java.util.List;

import com.example.crm.dto.request.Phone;

public class CustomerPhonesChangedEvent extends CustomerEvent {
    private List<Phone> phones;

    public CustomerPhonesChangedEvent() {
    }

    public CustomerPhonesChangedEvent(String customerIdentity, List<Phone> phones, long sequenceNumber) {
        super(customerIdentity, sequenceNumber);
        this.phones = phones == null ? List.of() : List.copyOf(phones);
    }

    @Override
    public CustomerEventType eventType() {
        return CustomerEventType.CUSTOMER_PHONE_CHANGED;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }
}
