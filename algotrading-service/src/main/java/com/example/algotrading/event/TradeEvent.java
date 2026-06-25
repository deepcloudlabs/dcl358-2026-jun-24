package com.example.algotrading.event;

import com.example.algotrading.document.TradeDocument;
import com.fasterxml.jackson.annotation.JsonProperty;

public record TradeEvent(
		@JsonProperty("s")  String symbol,
		@JsonProperty("p") double price,
		@JsonProperty("q") double quantity,
		@JsonProperty("T") long timestamp,
		@JsonProperty("t") long sequence,
		@JsonProperty("E") long tradeId) {

	public static TradeDocument toDocument(TradeEvent trade) {
		return new TradeDocument(
				trade.symbol(), 
				trade.price(), 
				trade.quantity(), 
				trade.timestamp(), 
				trade.sequence(), 
				trade.tradeId()
			);
	}

}
