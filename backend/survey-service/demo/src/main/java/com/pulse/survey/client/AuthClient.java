package com.pulse.survey.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "authentication-service", url = "${AUTH_SERVICE_URL:http://localhost:8080}", fallback = AuthClientFallback.class)
public interface AuthClient {

    @PostMapping("/api/auth/validate")
    ResponseEntity<TokenValidationResponse> validateToken(@RequestParam("token") String token);

    @GetMapping("/api/users/{id}")
    ResponseEntity<UserDto> getUserById(@PathVariable("id") String userId);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class TokenValidationResponse {
        private boolean valid;
        private String userId;
        private String username;
        private String role; // GLOBAL_HR, HR, EMPLOYEE
        private String location;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserDto {
        private String id;
        private String username;
        private String role;
        private String location;
    }
}
