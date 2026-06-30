package com.example.crm.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.crm.dto.request.Address;
import com.example.crm.dto.request.CreateCustomerRequest;
import com.example.crm.dto.request.Phone;
import com.example.crm.dto.request.UpdateCustomerRequest;
import com.example.crm.es.CustomerAddressesChangedEvent;
import com.example.crm.es.CustomerCreatedEvent;
import com.example.crm.es.CustomerEvent;
import com.example.crm.es.CustomerFullnameChangedEvent;
import com.example.crm.es.CustomerPhonesChangedEvent;
import com.example.crm.es.CustomerRemovedEvent;

public final class CustomerAggregate {
    public static final int MAX_ADDRESS_COUNT = 3;
    public static final int MAX_PHONE_COUNT = 3;

    private String identity;
    private String fullname;
    private List<Address> addresses = List.of();
    private List<Phone> phones = List.of();
    private boolean active;
    private long lastSequenceNumber;

    private CustomerAggregate() {
    }

    public static CustomerAggregate rehydrate(List<CustomerEvent> history) {
        var aggregate = new CustomerAggregate();
        history.forEach(aggregate::apply);
        return aggregate;
    }

    public CustomerCreatedEvent create(CreateCustomerRequest request) {
        requireInactive(request.identity());
        validateIdentity(request.identity());
        validateFullname(request.fullname());
        validateAddresses(request.addresses());
        validatePhones(request.phones());
        return new CustomerCreatedEvent(
                request.identity().trim(),
                request.fullname().trim(),
                copyOrEmpty(request.addresses()),
                copyOrEmpty(request.phones()),
                nextSequenceNumber());
    }

    public List<CustomerEvent> change(String identity, UpdateCustomerRequest request) {
        requireActive(identity);
        var events = new ArrayList<CustomerEvent>();
        var sequence = lastSequenceNumber;

        if (request.fullname() != null) {
            validateFullname(request.fullname());
            if (!Objects.equals(fullname, request.fullname().trim())) {
                events.add(new CustomerFullnameChangedEvent(identity, request.fullname().trim(), ++sequence));
            }
        }
        if (request.addresses() != null) {
            validateAddresses(request.addresses());
            if (!Objects.equals(addresses, request.addresses())) {
                events.add(new CustomerAddressesChangedEvent(identity, copyOrEmpty(request.addresses()), ++sequence));
            }
        }
        if (request.phones() != null) {
            validatePhones(request.phones());
            if (!Objects.equals(phones, request.phones())) {
                events.add(new CustomerPhonesChangedEvent(identity, copyOrEmpty(request.phones()), ++sequence));
            }
        }
        if (events.isEmpty()) {
            throw new BusinessException("Update request does not change customer state.");
        }
        return events;
    }

    public CustomerRemovedEvent remove(String identity) {
        requireActive(identity);
        return new CustomerRemovedEvent(identity, nextSequenceNumber());
    }

    private void apply(CustomerEvent event) {
        switch (event) {
            case CustomerCreatedEvent created -> {
                identity = created.getCustomerIdentity();
                fullname = created.getFullname();
                addresses = copyOrEmpty(created.getAddresses());
                phones = copyOrEmpty(created.getPhones());
                active = true;
            }
            case CustomerFullnameChangedEvent changed -> fullname = changed.getFullname();
            case CustomerAddressesChangedEvent changed -> addresses = copyOrEmpty(changed.getAddresses());
            case CustomerPhonesChangedEvent changed -> phones = copyOrEmpty(changed.getPhones());
            case CustomerRemovedEvent _ -> active = false;
            default -> throw new BusinessException("Unsupported event: " + event.getClass().getName());
        }
        lastSequenceNumber = Math.max(lastSequenceNumber, event.getSequenceNumber());
    }

    private void requireInactive(String candidateIdentity) {
        if (active) {
            throw new BusinessException("Customer already exists: " + candidateIdentity);
        }
    }

    private void requireActive(String expectedIdentity) {
        validateIdentity(expectedIdentity);
        if (!active || !Objects.equals(identity, expectedIdentity)) {
            throw new BusinessException("Customer does not exist or is not active: " + expectedIdentity);
        }
    }

    private long nextSequenceNumber() {
        return lastSequenceNumber + 1;
    }

    private static void validateIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            throw new BusinessException("Customer identity must not be blank.");
        }
    }

    private static void validateFullname(String fullname) {
        if (fullname == null || fullname.isBlank()) {
            throw new BusinessException("Customer fullname must not be blank.");
        }
    }

    private static void validateAddresses(List<Address> addresses) {
        if (addresses != null && addresses.size() > MAX_ADDRESS_COUNT) {
            throw new BusinessException("A customer can have at most %d addresses.".formatted(MAX_ADDRESS_COUNT));
        }
    }

    private static void validatePhones(List<Phone> phones) {
        if (phones != null && phones.size() > MAX_PHONE_COUNT) {
            throw new BusinessException("A customer can have at most %d phones.".formatted(MAX_PHONE_COUNT));
        }
    }

    private static <T> List<T> copyOrEmpty(List<T> source) {
        return source == null ? List.of() : List.copyOf(source);
    }
}
