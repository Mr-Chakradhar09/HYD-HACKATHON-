package com.inventory.warehouseservice.exception;

public class EmployeeAlreadyAssignedException extends RuntimeException {
    public EmployeeAlreadyAssignedException(String message) {
        super(message);
    }
}
