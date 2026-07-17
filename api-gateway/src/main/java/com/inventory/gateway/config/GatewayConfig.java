package com.inventory.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/v1/auth/**")
                        .uri("lb://auth-service"))
                .route("user-service", r -> r.path("/api/v1/users/**")
                        .uri("lb://auth-service"))
                .route("product-service", r -> r.path("/api/v1/products/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("default")))
                        .uri("lb://product-service"))
                .route("warehouse-service", r -> r.path("/api/v1/warehouses/**")
                        .uri("lb://warehouse-service"))
                .route("inventory-service", r -> r.path("/api/v1/inventory/**")
                        .filters(f -> f.circuitBreaker(c -> c.setName("default")))
                        .uri("lb://inventory-service"))
                .route("replenishment-service", r -> r.path("/api/v1/purchase-requests/**")
                        .uri("lb://replenishment-service"))
                .route("reporting-service", r -> r.path("/api/v1/reports/**")
                        .uri("lb://reporting-service"))
                .build();
    }
}
