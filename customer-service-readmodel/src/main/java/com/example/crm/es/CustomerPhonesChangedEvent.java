package com.example.crm.es;

import java.util.List;

import com.example.crm.document.Phone;

public class CustomerPhonesChangedEvent extends CustomerEvent {
    private List<Phone> phones;

    public CustomerPhonesChangedEvent() {
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
