package com.inventory.warehouseservice.feign;

import com.inventory.warehouseservice.config.FeignConfig;
import com.inventory.warehouseservice.dto.response.ApiResponse;
import com.inventory.warehouseservice.dto.response.InternalEmployeeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "auth-service", configuration = FeignConfig.class)
public interface AuthFeignClient {
    @GetMapping("/internal/employees")
    ApiResponse<List<InternalEmployeeResponse>> getAllEmployees();

    @GetMapping("/internal/employees/{employeeId}")
    ApiResponse<InternalEmployeeResponse> getEmployeeById(@PathVariable Long employeeId);

    @GetMapping("/internal/employees/code/{employeeCode}")
    ApiResponse<InternalEmployeeResponse> getEmployeeByCode(@PathVariable String employeeCode);
}