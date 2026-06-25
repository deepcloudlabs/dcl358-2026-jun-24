package com.example.cm.event;

import com.example.cm.domain.Address;

public class CustomerAddressChangedEvent extends CustomerManagementEvent {

	private Address address;

	public CustomerAddressChangedEvent(String customerId, Address address) {
		super(customerId);
		this.address = address;
	}

	public Address getAddress() {
		return address;
	}

}
