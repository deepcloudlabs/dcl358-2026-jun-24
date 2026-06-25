package com.example.cm.domain;

import java.util.Objects;

public class Address {
	private String line;
	private String city;
	private String country;

	public Address() {
	}

	public Address(String line, String city, String country) {
		this.line = line;
		this.city = city;
		this.country = country;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public int hashCode() {
		return Objects.hash(city, country, line);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		return Objects.equals(city, other.city) && Objects.equals(country, other.country)
				&& Objects.equals(line, other.line);
	}

	@Override
	public String toString() {
		return "Address [line=" + line + ", city=" + city + ", country=" + country + "]";
	}

}
