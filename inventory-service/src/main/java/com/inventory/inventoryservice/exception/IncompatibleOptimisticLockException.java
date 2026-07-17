package com.inventory.inventoryservice.exception;

public class IncompatibleOptimisticLockException extends RuntimeException {
    public IncompatibleOptimisticLockException(String message) { super(message); }
}
