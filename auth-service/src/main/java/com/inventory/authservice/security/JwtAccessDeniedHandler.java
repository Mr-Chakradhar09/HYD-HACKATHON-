package com.inventory.authservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.authservice.dto.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        ApiResponse<Object> apiResponse = new ApiResponse<>();

        apiResponse.setSuccess(false);
        apiResponse.setStatus(HttpStatus.FORBIDDEN.value());
        apiResponse.setMessage("Access Denied");
        apiResponse.setData(null);
        apiResponse.setPath(request.getRequestURI());
        apiResponse.setTimestamp(LocalDateTime.now());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
