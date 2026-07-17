package com.inventory.authservice.exception;

public class UnauthorizedRoleAssignmentException extends RuntimeException {
    public UnauthorizedRoleAssignmentException(String message) {
        super(message);
    }
}
