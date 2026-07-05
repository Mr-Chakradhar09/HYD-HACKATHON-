package com.virtusa.authservice.dto.response;

public class CurrentUserResponseDto {
    private String employeeId;
    private String email;
    private String role;
    private String location;

    public CurrentUserResponseDto() {
    }

    public CurrentUserResponseDto(String employeeId, String email, String role, String location) {
        this.employeeId = employeeId;
        this.email = email;
        this.role = role;
        this.location = location;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String employeeId;
        private String email;
        private String role;
        private String location;

        public Builder employeeId(String employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public CurrentUserResponseDto build() {
            return new CurrentUserResponseDto(employeeId, email, role, location);
        }
    }
}
