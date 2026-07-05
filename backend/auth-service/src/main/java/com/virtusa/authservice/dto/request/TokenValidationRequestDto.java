package com.virtusa.authservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public class TokenValidationRequestDto {
    @NotBlank(message = "Token is required")
    private String token;

    public TokenValidationRequestDto() {
    }

    public TokenValidationRequestDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
