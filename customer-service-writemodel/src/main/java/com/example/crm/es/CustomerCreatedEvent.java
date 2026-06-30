package com.example.crm.es;

import java.util.List;

import com.example.crm.dto.request.Address;
import com.example.crm.dto.request.Phone;

public class CustomerCreatedEvent extends CustomerEvent {
    private String fullname;
    private List<Address> addresses;
    private List<Phone> phones;

    public CustomerCreatedEvent() {
    }

    public CustomerCreatedEvent(String customerIdentity, String fullname, List<Address> addresses, List<Phone> phones,
            long sequenceNumber) {
        super(customerIdentity, sequenceNumber);
        this.fullname = fullname;
        this.addresses = addresses == null ? List.of() : List.copyOf(addresses);
        this.phones = phones == null ? List.of() : List.copyOf(phones);
    }

    @Override
    public CustomerEventType eventType() {
        return CustomerEventType.CUSTOMER_CREATED;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }
}
