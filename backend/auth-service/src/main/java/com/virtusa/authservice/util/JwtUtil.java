package com.virtusa.authservice.util;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
    
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}
