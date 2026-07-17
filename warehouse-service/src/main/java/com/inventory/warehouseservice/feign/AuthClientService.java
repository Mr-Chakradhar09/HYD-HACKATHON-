package com.inventory.warehouseservice.feign;

import com.inventory.warehouseservice.dto.response.InternalEmployeeResponse;
import com.inventory.warehouseservice.enums.WarehouseRole;
import com.inventory.warehouseservice.exception.EmployeeInactiveException;
import com.inventory.warehouseservice.exception.EmployeeNotFoundException;
import com.inventory.warehouseservice.exception.InvalidWarehouseRoleException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthClientService {

    private static final Logger log = LoggerFactory.getLogger(AuthClientService.class);
    private final AuthFeignClient authFeignClient;

    public AuthClientService(AuthFeignClient authFeignClient) {
        this.authFeignClient = authFeignClient;
    }

    @CircuitBreaker(name = "authService", fallbackMethod = "getEmployeeByIdFallback")
    @Retry(name = "authService", fallbackMethod = "getEmployeeByIdFallback")
    @Bulkhead(name = "authService", fallbackMethod = "getEmployeeByIdFallback")
    @RateLimiter(name = "authService", fallbackMethod = "getEmployeeByIdFallback")
    @TimeLimiter(name = "authService", fallbackMethod = "getEmployeeByIdFallback")
    public InternalEmployeeResponse getEmployeeById(Long employeeId) {
        log.debug("Calling auth-service for employee: {}", employeeId);
        var response = authFeignClient.getEmployeeById(employeeId);
        if (response == null || response.getData() == null) {
            throw new EmployeeNotFoundException("Employee not found");
        }
        InternalEmployeeResponse employee = response.getData();
        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new EmployeeInactiveException("Employee is inactive");
        }
        return employee;
    }

    public InternalEmployeeResponse getEmployeeByIdFallback(Long employeeId, Throwable throwable) {
        log.warn("Fallback triggered for getEmployeeById({}): {}", employeeId, throwable.getMessage());
        throw new EmployeeNotFoundException("Auth service unavailable. Cannot verify employee: " + employeeId);
    }

    public void validateEmployeeRole(InternalEmployeeResponse employee, WarehouseRole warehouseRole) {
        if (!employee.getRoles().contains(warehouseRole.name())) {
            throw new InvalidWarehouseRoleException("Employee does not have role : " + warehouseRole);
        }
    }
}
