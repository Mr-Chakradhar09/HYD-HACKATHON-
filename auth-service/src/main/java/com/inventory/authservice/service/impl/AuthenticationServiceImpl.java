package com.inventory.authservice.service.impl;

import com.inventory.authservice.dto.request.LoginRequest;
import com.inventory.authservice.dto.request.RefreshTokenRequest;
import com.inventory.authservice.dto.response.LoginResponse;
import com.inventory.authservice.dto.response.RefreshTokenResponse;
import com.inventory.authservice.entity.RefreshToken;
import com.inventory.authservice.entity.User;
import com.inventory.authservice.exception.InvalidTokenException;
import com.inventory.authservice.exception.UserNotFoundException;
import com.inventory.authservice.repository.UserRepository;
import com.inventory.authservice.security.JwtService;
import com.inventory.authservice.service.AuthenticationService;
import com.inventory.authservice.service.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationServiceImpl(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtService jwtService,
            RefreshTokenService refreshTokenService) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmployeeCode(),
                        request.getPassword()));

        User user = userRepository.findByEmployee_EmployeeCode(request.getEmployeeCode())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken.getToken());
        response.setEmployeeCode(user.getEmployee().getEmployeeCode());
        response.setUsername(user.getUsername());
        response.setFullName(user.getEmployee().getFullName());
        response.setRoles(
                user.getEmployee()
                        .getRoles()
                        .stream()
                        .map(role -> role.getRoleName().name())
                        .toList()
        );
        return response;
    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return new RefreshTokenResponse(newAccessToken, refreshToken.getToken());
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeRefreshToken(refreshToken);
    }
}
