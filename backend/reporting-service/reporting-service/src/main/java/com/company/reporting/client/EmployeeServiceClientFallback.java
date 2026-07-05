package com.company.reporting.client;

import org.springframework.stereotype.Component;

@Component
public class EmployeeServiceClientFallback implements EmployeeServiceClient {

    @Override
    public EmployeeResponse getEmployeeById(Long id) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(id);
        response.setName("Fallback Employee");
        response.setEmail("fallback@company.com");
        response.setCity("Chennai");
        response.setState("Tamil Nadu");
        response.setCountry("India");
        response.setRegion("APAC");
        response.setBusinessUnit("Engineering");
        response.setDepartment("Engineering");
        response.setManagerId(999L);
        return response;
    }
}
