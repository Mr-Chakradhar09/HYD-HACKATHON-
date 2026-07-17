package com.inventory.warehouseservice.dto.response;
import java.util.Set;

public class InternalEmployeeResponse {

    private Long employeeId;

    private String employeeCode;

    private String fullName;

    private String email;

    private String status;

    private Set<String> roles;


    public InternalEmployeeResponse() {
    }


    public InternalEmployeeResponse(
            Long employeeId,
            String employeeCode,
            String fullName,
            String email,
            String status,
            Set<String> roles) {

        this.employeeId = employeeId;
        this.employeeCode = employeeCode;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
        this.roles = roles;
    }


    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }


    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
