package com.virtusa.authservice.service;

import com.virtusa.authservice.dto.request.ChangePasswordRequestDto;
import com.virtusa.authservice.dto.request.CreateCredentialRequestDto;
import com.virtusa.authservice.dto.request.LoginRequestDto;
import com.virtusa.authservice.dto.request.TokenValidationRequestDto;
import com.virtusa.authservice.dto.response.CurrentUserResponseDto;
import com.virtusa.authservice.dto.response.LoginResponseDto;
import com.virtusa.authservice.dto.response.TokenValidationResponseDto;

public interface AuthService {
    void createCredentials(CreateCredentialRequestDto requestDto);
    LoginResponseDto login(LoginRequestDto requestDto);
    TokenValidationResponseDto validateToken(TokenValidationRequestDto requestDto);
    CurrentUserResponseDto getCurrentUser(String email);
    void changePassword(String email, ChangePasswordRequestDto requestDto);
    void resetPassword(String employeeId, String newPassword);
}
