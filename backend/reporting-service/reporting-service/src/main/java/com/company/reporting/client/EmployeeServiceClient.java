package com.company.reporting.client;

import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "employee-service", url = "${app.services.employee-service-url:http://localhost:8081}", fallback = com.company.reporting.client.EmployeeServiceClientFallback.class)
public interface EmployeeServiceClient {

    @GetMapping("/api/employees/{id}")
    EmployeeResponse getEmployeeById(@PathVariable("id") Long id);

    @Data
    class EmployeeResponse {
        private Long id;
        private String name;
        private String email;
        private String city;
        private String state;
        private String country;
        private String region;
        private String businessUnit;
        private String department;
        private Long managerId;
    }
}
