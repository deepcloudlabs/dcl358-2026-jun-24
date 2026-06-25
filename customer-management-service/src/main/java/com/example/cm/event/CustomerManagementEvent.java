package com.example.cm.event;

import java.time.ZonedDateTime;
import java.util.UUID;

public abstract class CustomerManagementEvent {
	private final String eventId = UUID.randomUUID().toString();
	private final Long timestamp = ZonedDateTime.now().toEpochSecond();
	private final String customerId;

	public CustomerManagementEvent(String customerId) {
		this.customerId = customerId;
	}

	public String getEventId() {
		return eventId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public String getCustomerId() {
		return customerId;
	}

}
