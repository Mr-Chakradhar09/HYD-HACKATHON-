package com.inventory.warehouseservice.util;

import com.inventory.warehouseservice.enums.WarehouseRole;

public final class RoleHierarchyUtil {

    private RoleHierarchyUtil() {
    }

    public static boolean canAssign(WarehouseRole creator, WarehouseRole target) {
        switch (creator) {
            case SYSTEM_ADMIN:
                return true;
            case INVENTORY_MANAGER:
                return target == WarehouseRole.PROCUREMENT_MANAGER
                        || target == WarehouseRole.WAREHOUSE_OPERATOR;
            case PROCUREMENT_MANAGER:
            case WAREHOUSE_OPERATOR:
                return false;
            default:
                return false;
        }
    }
}
