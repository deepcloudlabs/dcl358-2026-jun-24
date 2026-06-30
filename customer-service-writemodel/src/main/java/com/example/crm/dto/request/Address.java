package com.example.crm.dto.request;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Address {
    @NotNull
    private AddressType type;
    @NotBlank
    private String country;
    @NotBlank
    private String city;
    @NotBlank
    private String line;
    @NotBlank
    private String zipCode;

    public Address() {
    }

    public AddressType getType() {
        return type;
    }

    public void setType(AddressType type) {
        this.type = type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(city, country, line, type, zipCode);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Address other)) {
            return false;
        }
        return Objects.equals(city, other.city)
                && Objects.equals(country, other.country)
                && Objects.equals(line, other.line)
                && type == other.type
                && Objects.equals(zipCode, other.zipCode);
    }

    @Override
    public String toString() {
        return "Address[type=%s, country=%s, city=%s, line=%s, zipCode=%s]"
                .formatted(type, country, city, line, zipCode);
    }
}
