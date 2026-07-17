package com.inventory.warehouseservice.enums;

/**
 * Roles that can be assigned to employees in a warehouse.
 * Must match the RoleType enum in auth-service exactly.
 */
public enum WarehouseRole {

    SYSTEM_ADMIN,
    INVENTORY_MANAGER,
    WAREHOUSE_OPERATOR,
    PROCUREMENT_MANAGER

}
