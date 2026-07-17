package com.inventory.warehouseservice.util;

import com.inventory.warehouseservice.enums.WarehouseRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.stream.Collectors;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static String getCurrentEmployeeCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof String) {
            return (String) principal;
        }
        return authentication.getName();
    }

    public static Set<WarehouseRole> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .filter(a -> a instanceof SimpleGrantedAuthority)
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .map(roleName -> {
                    try {
                        return WarehouseRole.valueOf(roleName);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(role -> role != null)
                .collect(Collectors.toSet());
    }

    public static WarehouseRole getHighestRole() {
        Set<WarehouseRole> roles = getCurrentUserRoles();
        if (roles.contains(WarehouseRole.SYSTEM_ADMIN)) return WarehouseRole.SYSTEM_ADMIN;
        if (roles.contains(WarehouseRole.INVENTORY_MANAGER)) return WarehouseRole.INVENTORY_MANAGER;
        if (roles.contains(WarehouseRole.PROCUREMENT_MANAGER)) return WarehouseRole.PROCUREMENT_MANAGER;
        if (roles.contains(WarehouseRole.WAREHOUSE_OPERATOR)) return WarehouseRole.WAREHOUSE_OPERATOR;
        return null;
    }
}
