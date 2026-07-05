package com.pulse.survey.client;

import com.pulse.survey.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthClientFallback implements AuthClient {

    @Override
    public ResponseEntity<TokenValidationResponse> validateToken(String token) {
        log.error("AuthClient fallback triggered for validateToken. Authentication Service is down.");
        // Return a response showing token is invalid due to fallback
        TokenValidationResponse fallbackRes = TokenValidationResponse.builder()
                .valid(false)
                .build();
        return ResponseEntity.ok(fallbackRes);
    }

    @Override
    public ResponseEntity<UserDto> getUserById(String userId) {
        log.error("AuthClient fallback triggered for getUserById. Authentication Service is down.");
        throw new BadRequestException("Authentication Service is currently unavailable. Please try again later.");
    }
}
