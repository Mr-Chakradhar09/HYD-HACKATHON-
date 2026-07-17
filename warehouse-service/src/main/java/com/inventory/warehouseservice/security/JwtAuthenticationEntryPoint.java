package com.inventory.warehouseservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.warehouseservice.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(false);
        apiResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        apiResponse.setMessage("Authentication Failed");
        apiResponse.setData(null);
        apiResponse.setPath(request.getRequestURI());
        apiResponse.setTimestamp(LocalDateTime.now());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}