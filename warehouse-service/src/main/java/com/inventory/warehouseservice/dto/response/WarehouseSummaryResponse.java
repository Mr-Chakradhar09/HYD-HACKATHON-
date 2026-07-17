package com.inventory.warehouseservice.dto.response;

import com.inventory.warehouseservice.enums.CapacityUnit;
import com.inventory.warehouseservice.enums.WarehouseStatus;

public class WarehouseSummaryResponse {

    private Long id;
    private String warehouseCode;
    private String warehouseName;
    private String city;
    private Integer capacity;
    private CapacityUnit capacityUnit;
    private AddressResponse address;
    private WarehouseStatus status;

    public WarehouseSummaryResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public CapacityUnit getCapacityUnit() {
        return capacityUnit;
    }

    public void setCapacityUnit(CapacityUnit capacityUnit) {
        this.capacityUnit = capacityUnit;
    }

    public AddressResponse getAddress() {
        return address;
    }

    public void setAddress(AddressResponse address) {
        this.address = address;
    }

    public WarehouseStatus getStatus() {
        return status;
    }

    public void setStatus(WarehouseStatus status) {
        this.status = status;
    }
}
