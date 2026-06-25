package com.example.cm.event;

import com.example.cm.domain.Address;

public class CustomerAddressChangedEvent extends CustomerManagementEvent {

	public CustomerAddressChangedEvent(String customerId,Address address) {
		super(customerId);
		// TODO Auto-generated constructor stub
	}

}
