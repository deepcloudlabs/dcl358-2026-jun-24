package com.example.crm.document;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customers")
public class CustomerReadModel {
    @Id
    private String identity;
    @Indexed
    private String fullname;
    private List<Address> addresses = List.of();
    private List<Phone> phones = List.of();
    private long lastSequenceNumber;
    private Instant lastUpdatedAt;

    public CustomerReadModel() {
    }

    public CustomerReadModel(String identity, String fullname, List<Address> addresses, List<Phone> phones,
            long lastSequenceNumber, Instant lastUpdatedAt) {
        this.identity = identity;
        this.fullname = fullname;
        this.addresses = addresses == null ? List.of() : List.copyOf(addresses);
        this.phones = phones == null ? List.of() : List.copyOf(phones);
        this.lastSequenceNumber = lastSequenceNumber;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
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
        this.addresses = addresses == null ? List.of() : List.copyOf(addresses);
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones == null ? List.of() : List.copyOf(phones);
    }

    public long getLastSequenceNumber() {
        return lastSequenceNumber;
    }

    public void setLastSequenceNumber(long lastSequenceNumber) {
        this.lastSequenceNumber = lastSequenceNumber;
    }

    public Instant getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Instant lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public boolean alreadyApplied(long sequenceNumber) {
        return sequenceNumber <= lastSequenceNumber;
    }

    public void markApplied(long sequenceNumber, Instant occurredAt) {
        this.lastSequenceNumber = sequenceNumber;
        this.lastUpdatedAt = occurredAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CustomerReadModel other)) {
            return false;
        }
        return Objects.equals(identity, other.identity);
    }

    @Override
    public String toString() {
        return "CustomerReadModel[identity=%s, fullname=%s, lastSequenceNumber=%d]"
                .formatted(identity, fullname, lastSequenceNumber);
    }
}
