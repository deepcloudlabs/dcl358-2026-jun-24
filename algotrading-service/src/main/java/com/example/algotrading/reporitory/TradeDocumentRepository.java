package com.example.algotrading.reporitory;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.algotrading.document.TradeDocument;

public interface TradeDocumentRepository extends MongoRepository<TradeDocument, Long>{

}
