package com.example.crm.es;

import java.util.List;

import com.example.crm.document.Address;

public class CustomerAddressesChangedEvent extends CustomerEvent {
    private List<Address> addresses;

    public CustomerAddressesChangedEvent() {
    }

    @Override
    public CustomerEventType eventType() {
        return CustomerEventType.CUSTOMER_ADDRESS_CHANGED;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
}
