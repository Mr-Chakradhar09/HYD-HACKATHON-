package com.inventory.warehouse.dto;

import com.inventory.warehouse.entity.Warehouse;

public class WarehouseResponse {
    private Long id;
    private String code;
    private String name;
    private String city;
    private String state;
    private String country;
    private Integer capacity;
    private String status;

    public static WarehouseResponse fromEntity(Warehouse w) {
        WarehouseResponse r = new WarehouseResponse();
        r.id = w.getId(); r.code = w.getCode(); r.name = w.getName();
        r.city = w.getCity(); r.state = w.getState(); r.country = w.getCountry();
        r.capacity = w.getCapacity(); r.status = w.getStatus();
        return r;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getCountry() { return country; }
    public Integer getCapacity() { return capacity; }
    public String getStatus() { return status; }
}
