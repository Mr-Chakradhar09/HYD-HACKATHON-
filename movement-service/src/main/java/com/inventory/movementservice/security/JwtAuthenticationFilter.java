package com.inventory.movementservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtValidator jwtValidator;
    public JwtAuthenticationFilter(JwtValidator jwtValidator) { this.jwtValidator = jwtValidator; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String employeeCode = request.getHeader("X-User-Id");
        String rolesHeader = request.getHeader("X-User-Roles");

        if (employeeCode != null && rolesHeader != null) {
            List<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r.trim())).toList();
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(employeeCode, null, authorities));
        } else {
            String token = extractToken(request);
            if (token != null) {
                try {
                    if (jwtValidator.isTokenValid(token)) {
                        List<SimpleGrantedAuthority> authorities = jwtValidator.extractRoles(token).stream()
                                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.trim())).toList();
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(jwtValidator.extractEmployeeCode(token), null, authorities));
                    }
                } catch (Exception ignored) {
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String h = request.getHeader("Authorization");
        return StringUtils.hasText(h) && h.startsWith("Bearer ") ? h.substring(7) : null;
    }
}
