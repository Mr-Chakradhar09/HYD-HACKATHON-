package com.inventory.productservice.exception;

public class IncompatibleOptimisticLockException extends RuntimeException {
    public IncompatibleOptimisticLockException(String message) {
        super(message);
    }
}
