package com.inventory.warehouseservice.exception;

public class WarehouseAssignmentNotFoundException extends RuntimeException {
    public WarehouseAssignmentNotFoundException(String message) {
        super(message);
    }
}
