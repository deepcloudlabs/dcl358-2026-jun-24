package com.example.crm.dto.request;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Phone {
    @NotNull
    private PhoneType type;
    @NotBlank
    private String countryCode;
    @NotBlank
    private String number;

    public Phone() {
    }

    public PhoneType getType() {
        return type;
    }

    public void setType(PhoneType type) {
        this.type = type;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, number, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Phone other)) {
            return false;
        }
        return Objects.equals(countryCode, other.countryCode)
                && Objects.equals(number, other.number)
                && type == other.type;
    }

    @Override
    public String toString() {
        return "Phone[type=%s, countryCode=%s, number=%s]".formatted(type, countryCode, number);
    }
}
