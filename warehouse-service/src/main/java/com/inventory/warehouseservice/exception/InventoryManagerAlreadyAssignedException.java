package com.inventory.warehouseservice.exception;

public class InventoryManagerAlreadyAssignedException extends RuntimeException {
    public InventoryManagerAlreadyAssignedException(String message) {
        super(message);
    }
}
