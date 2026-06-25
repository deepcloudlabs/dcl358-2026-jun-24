package com.example.algotrading.document;

import java.util.Objects;

import org.springframework.data.annotation.Id;

public class TradeDocument {
	private String symbol;
	private double price;
	private double quantity;
	private long timestamp;
	@Id
	private long sequence;
	private long tradeId;

	public TradeDocument() {
	}

	public TradeDocument(String symbol, double price, double quantity, long timestamp, long sequence, long tradeId) {
		this.symbol = symbol;
		this.price = price;
		this.quantity = quantity;
		this.timestamp = timestamp;
		this.sequence = sequence;
		this.tradeId = tradeId;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getSequence() {
		return sequence;
	}

	public void setSequence(long sequence) {
		this.sequence = sequence;
	}

	public long getTradeId() {
		return tradeId;
	}

	public void setTradeId(long tradeId) {
		this.tradeId = tradeId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(Long.valueOf(sequence), symbol, Long.valueOf(tradeId));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TradeDocument other = (TradeDocument) obj;
		return sequence == other.sequence && Objects.equals(symbol, other.symbol) && tradeId == other.tradeId;
	}

	@Override
	public String toString() {
		return "TradeDocument [symbol=" + symbol + ", price=" + price + ", quantity=" + quantity + ", timestamp="
				+ timestamp + ", sequence=" + sequence + ", tradeId=" + tradeId + "]";
	}

}
