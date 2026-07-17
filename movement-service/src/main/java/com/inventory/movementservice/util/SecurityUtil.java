package com.inventory.movementservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.stream.Collectors;

public final class SecurityUtil {

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

    public static Set<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }
        return authentication.getAuthorities().stream()
                .filter(a -> a instanceof SimpleGrantedAuthority)
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());
    }

    public static boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }
}
