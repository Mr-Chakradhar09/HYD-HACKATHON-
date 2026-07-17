package com.inventory.warehouseservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AddressRequest {

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    private String country;

    @NotBlank
    private String postalCode;

    public AddressRequest() {
    }

    public AddressRequest(String addressLine1, String postalCode, String country, String state, String city, String addressLine2) {
        this.addressLine1 = addressLine1;
        this.postalCode = postalCode;
        this.country = country;
        this.state = state;
        this.city = city;
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
// Getters & Setters
}