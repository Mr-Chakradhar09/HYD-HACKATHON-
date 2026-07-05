package com.virtusa.authservice.dto.response;

public class TokenValidationResponseDto {
    private boolean valid;
    private String employeeId;
    private String email;
    private String role;
    private String location;

    public TokenValidationResponseDto() {
    }

    public TokenValidationResponseDto(boolean valid, String employeeId, String email, String role, String location) {
        this.valid = valid;
        this.employeeId = employeeId;
        this.email = email;
        this.role = role;
        this.location = location;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
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
        private boolean valid;
        private String employeeId;
        private String email;
        private String role;
        private String location;

        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

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

        public TokenValidationResponseDto build() {
            return new TokenValidationResponseDto(valid, employeeId, email, role, location);
        }
    }
}
