package com.example.crm.es;

import java.util.List;

import com.example.crm.dto.request.Address;

public class CustomerAddressesChangedEvent extends CustomerEvent {
    private List<Address> addresses;

    public CustomerAddressesChangedEvent() {
    }

    public CustomerAddressesChangedEvent(String customerIdentity, List<Address> addresses, long sequenceNumber) {
        super(customerIdentity, sequenceNumber);
        this.addresses = addresses == null ? List.of() : List.copyOf(addresses);
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
