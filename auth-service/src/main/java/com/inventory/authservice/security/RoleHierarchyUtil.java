package com.inventory.authservice.security;

import com.inventory.authservice.enums.RoleType;

public final class RoleHierarchyUtil {

    private RoleHierarchyUtil() {
    }


    public static boolean canAssign(RoleType creator, RoleType target) {
        switch (creator) {
            case SYSTEM_ADMIN:
                return true;
            case INVENTORY_MANAGER:
                return target == RoleType.PROCUREMENT_MANAGER || target == RoleType.WAREHOUSE_OPERATOR;
            case PROCUREMENT_MANAGER:
            case WAREHOUSE_OPERATOR:
                return false;
            default:
                return false;
        }
    }
}