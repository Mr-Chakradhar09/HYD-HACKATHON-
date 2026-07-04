package com.inventory.warehouse.dto;

import jakarta.validation.constraints.NotBlank;

public class WarehouseRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    private String city;
    private String state;
    private String country;
    private Integer capacity;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}
