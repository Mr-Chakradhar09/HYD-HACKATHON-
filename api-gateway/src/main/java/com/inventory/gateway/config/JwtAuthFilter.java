package com.inventory.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:MySecretKeyForInventoryManagementSystem2024!VeryLongAndSecure}")
    private String secret;

    private static final List<String> ALL_ROLES = List.of("SYSTEM_ADMIN", "INVENTORY_MANAGER", "WAREHOUSE_MANAGER", "PROCUREMENT_MANAGER");

    private static final Map<String, List<String>> PATH_ROLES = Map.of(
        "/api/v1/users", List.of("SYSTEM_ADMIN"),
        "/api/v1/warehouses", List.of("SYSTEM_ADMIN", "INVENTORY_MANAGER", "WAREHOUSE_MANAGER", "PROCUREMENT_MANAGER"),
        "/api/v1/products", ALL_ROLES,
        "/api/v1/inventory", List.of("SYSTEM_ADMIN", "INVENTORY_MANAGER", "WAREHOUSE_MANAGER"),
        "/api/v1/purchase-requests", List.of("SYSTEM_ADMIN", "PROCUREMENT_MANAGER", "INVENTORY_MANAGER", "WAREHOUSE_MANAGER"),
        "/api/v1/reports", List.of("SYSTEM_ADMIN", "INVENTORY_MANAGER", "PROCUREMENT_MANAGER", "WAREHOUSE_MANAGER")
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.equals("/api/v1/auth/login")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String role = claims.get("role", String.class);
            Long warehouseId = claims.get("warehouseId", Long.class);

            exchange = exchange.mutate()
                    .request(r -> r.header("X-User-Id", claims.getSubject())
                            .header("X-User-Role", role)
                            .header("X-Warehouse-Id", warehouseId != null ? warehouseId.toString() : ""))
                    .build();

            String matchedPath = PATH_ROLES.keySet().stream()
                    .filter(path::startsWith)
                    .findFirst()
                    .orElse(null);

            if (matchedPath != null) {
                List<String> allowedRoles = PATH_ROLES.get(matchedPath);
                if (!allowedRoles.contains(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
