package com.inventory.warehouseservice.exception;

public class WarehouseAlreadyExistsException extends RuntimeException {
  public WarehouseAlreadyExistsException(String message) {
    super(message);
  }
}
