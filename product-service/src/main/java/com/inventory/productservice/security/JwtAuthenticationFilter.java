package com.inventory.productservice.security;

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
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;

    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String employeeCode = request.getHeader("X-User-Id");
        String rolesHeader = request.getHeader("X-User-Roles");

        if (employeeCode != null && rolesHeader != null) {
            List<String> roles = java.util.Arrays.asList(rolesHeader.split(","));
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                    .toList();
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(employeeCode, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            String token = extractToken(request);
            if (token != null) {
                try {
                    if (jwtValidator.isTokenValid(token)) {
                        String jwtEmployeeCode = jwtValidator.extractEmployeeCode(token);
                        List<String> jwtRoles = jwtValidator.extractRoles(token);
                        List<SimpleGrantedAuthority> authorities = jwtRoles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                                .toList();
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(jwtEmployeeCode, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
