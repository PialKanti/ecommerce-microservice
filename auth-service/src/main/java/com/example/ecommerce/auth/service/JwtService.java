package com.example.ecommerce.auth.service;

import com.example.ecommerce.auth.domain.AuthUserPrincipal;

import java.time.Duration;

public interface JwtService {
    String generateAccessToken(AuthUserPrincipal principal);

    String extractUsername(String token);

    boolean isTokenValid(String token);

    Duration getRemainingLifetime(String token);

    Duration getAccessTokenExpiration();
}
