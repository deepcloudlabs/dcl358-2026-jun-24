package com.example.crm.es;

public class CustomerFullnameChangedEvent extends CustomerEvent {
    private String fullname;

    public CustomerFullnameChangedEvent() {
    }

    @Override
    public CustomerEventType eventType() {
        return CustomerEventType.CUSTOMER_FULLNAME_CHANGED;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
