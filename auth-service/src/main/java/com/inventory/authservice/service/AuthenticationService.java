package com.inventory.authservice.service;

import com.inventory.authservice.dto.request.LoginRequest;
import com.inventory.authservice.dto.request.RefreshTokenRequest;
import com.inventory.authservice.dto.response.LoginResponse;
import com.inventory.authservice.dto.response.RefreshTokenResponse;

public interface AuthenticationService {

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);
}