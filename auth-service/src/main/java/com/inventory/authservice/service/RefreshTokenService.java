package com.inventory.authservice.service;

import com.inventory.authservice.entity.RefreshToken;
import com.inventory.authservice.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken validateRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllRefreshTokensForUser(Long userId);
}
