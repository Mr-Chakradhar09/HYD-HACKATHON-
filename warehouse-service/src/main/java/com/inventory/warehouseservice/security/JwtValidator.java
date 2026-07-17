package com.inventory.warehouseservice.security;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Thin facade over JwtService, used by JwtAuthenticationFilter.
 */
@Component
public class JwtValidator {

    private final JwtService jwtService;

    public JwtValidator(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public boolean isTokenValid(String token) {
        return jwtService.isTokenValid(token);
    }

    public List<String> extractRoles(String token) {
        return jwtService.extractRoles(token);
    }

    public String extractEmployeeCode(String token) {
        return jwtService.extractEmployeeCode(token);
    }
}
