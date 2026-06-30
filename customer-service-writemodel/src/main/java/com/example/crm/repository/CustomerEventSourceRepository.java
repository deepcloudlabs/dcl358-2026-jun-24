package com.example.crm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.crm.es.CustomerEvent;

public interface CustomerEventSourceRepository extends MongoRepository<CustomerEvent, String> {
    List<CustomerEvent> findByCustomerIdentityOrderBySequenceNumberAsc(String customerIdentity);

    Optional<CustomerEvent> findTopByCustomerIdentityOrderBySequenceNumberDesc(String customerIdentity);
}
