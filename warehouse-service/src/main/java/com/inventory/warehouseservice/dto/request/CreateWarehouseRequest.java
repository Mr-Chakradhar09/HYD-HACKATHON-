package com.inventory.warehouseservice.dto.request;
import com.inventory.warehouseservice.enums.CapacityUnit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class CreateWarehouseRequest {

    @NotBlank(message = "Warehouse code is required")
    @Size(min = 12, max = 12, message = "Warehouse code must be exactly 12 characters")
    @Pattern(regexp = "^WAHO\\d{8}$", message = "Warehouse code must be in format WAHO11223344 (WAHO followed by 8 digits)")
    private String warehouseCode;

    @NotBlank(message = "Warehouse name is required")
    @Size(max = 100)
    private String warehouseName;

    @Email(message = "Invalid email")
    private String email;

    private String phone;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be greater than zero")
    private Integer capacity;

    private CapacityUnit capacityUnit;

    @NotNull(message = "Address is required")
    @Valid
    private AddressRequest address;

    public CreateWarehouseRequest() {
    }

    public CreateWarehouseRequest(String warehouseCode, String warehouseName, String email, String phone, Integer capacity, AddressRequest address) {
        this.warehouseCode = warehouseCode;
        this.warehouseName = warehouseName;
        this.email = email;
        this.phone = phone;
        this.capacity = capacity;
        this.address = address;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
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

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public CapacityUnit getCapacityUnit() {
        return capacityUnit;
    }

    public void setCapacityUnit(CapacityUnit capacityUnit) {
        this.capacityUnit = capacityUnit;
    }
// Getters & Setters
}