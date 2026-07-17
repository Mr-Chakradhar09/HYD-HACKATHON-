package com.inventory.warehouseservice.dto.request;

import com.inventory.warehouseservice.enums.CapacityUnit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class UpdateWarehouseRequest {

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 100)
    private String warehouseName;

    @Email(message = "Invalid email")
    private String email;

    private String phone;

    @NotNull
    @Positive
    private Integer capacity;

    private CapacityUnit capacityUnit;

    @NotNull
    @Valid
    private AddressRequest address;

    public UpdateWarehouseRequest() {
    }

    public UpdateWarehouseRequest(String warehouseName, AddressRequest address, Integer capacity, CapacityUnit capacityUnit, String phone, String email) {
        this.warehouseName = warehouseName;
        this.address = address;
        this.capacity = capacity;
        this.capacityUnit = capacityUnit;
        this.phone = phone;
        this.email = email;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public AddressRequest getAddress() {
        return address;
    }

    public void setAddress(AddressRequest address) {
        this.address = address;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public CapacityUnit getCapacityUnit() {
        return capacityUnit;
    }

    public void setCapacityUnit(CapacityUnit capacityUnit) {
        this.capacityUnit = capacityUnit;
    }
// Getters & Setters
}