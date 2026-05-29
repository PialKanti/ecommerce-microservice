package com.example.ecommerce.auth.service.impl;

import com.example.ecommerce.auth.exception.AuthenticationFailedException;
import com.example.ecommerce.auth.exception.InvalidTokenException;
import com.example.ecommerce.commons.exception.ResourceConflictException;
import com.example.ecommerce.auth.config.JwtProperties;
import com.example.ecommerce.auth.domain.AuthUserPrincipal;
import com.example.ecommerce.auth.dto.request.LoginRequest;
import com.example.ecommerce.auth.dto.request.RefreshTokenRequest;
import com.example.ecommerce.auth.dto.request.RegisterRequest;
import com.example.ecommerce.auth.dto.response.AuthResponse;
import com.example.ecommerce.auth.dto.response.UserResponse;
import com.example.ecommerce.auth.entity.RefreshToken;
import com.example.ecommerce.auth.entity.Role;
import com.example.ecommerce.auth.entity.User;
import com.example.ecommerce.commons.enums.RoleCode;
import com.example.ecommerce.auth.mapper.UserMapper;
import com.example.ecommerce.auth.repository.RefreshTokenRepository;
import com.example.ecommerce.auth.repository.RoleRepository;
import com.example.ecommerce.auth.repository.UserRepository;
import com.example.ecommerce.auth.service.AuthService;
import com.example.ecommerce.auth.service.JwtService;
import com.example.ecommerce.auth.service.TokenBlacklistService;
import com.example.ecommerce.auth.util.TokenHashUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE     = "Bearer";
    private static final String BEARER_PREFIX  = "Bearer ";
    private static final int    REFRESH_TOKEN_BYTES = 64;

    private final PasswordEncoder          passwordEncoder;
    private final UserRepository           userRepository;
    private final RefreshTokenRepository   refreshTokenRepository;
    private final RoleRepository           roleRepository;
    private final UserMapper               userMapper;
    private final JwtService               jwtService;
    private final TokenBlacklistService    tokenBlacklistService;
    private final JwtProperties            jwtProperties;
    private final SecureRandom             secureRandom = new SecureRandom();

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceConflictException("User with username '" + request.username() + "' already exists.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceConflictException("User with email '" + request.email() + "' already exists.");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setIsActive(Boolean.TRUE);
        user.getRoles().add(defaultCustomerRole());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new AuthenticationFailedException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        AuthUserPrincipal principal = buildPrincipal(user);
        String accessToken  = jwtService.generateAccessToken(principal);
        String refreshToken = createRefreshToken(user);
        return authResponse(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = getValidRefreshToken(request.refreshToken());
        refreshToken.setRevoked(Boolean.TRUE);
        refreshTokenRepository.save(refreshToken);

        User user = userRepository.findByUsername(refreshToken.getUser().getUsername())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        AuthUserPrincipal principal = buildPrincipal(user);
        String accessToken    = jwtService.generateAccessToken(principal);
        String newRefreshToken = createRefreshToken(user);
        return authResponse(accessToken, newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String bearerToken, RefreshTokenRequest request) {
        String accessToken = extractBearerToken(bearerToken);
        Duration ttl;
        try {
            ttl = jwtService.getRemainingLifetime(accessToken);
        } catch (RuntimeException exception) {
            throw new InvalidTokenException("Access token is invalid.");
        }
        tokenBlacklistService.blacklist(accessToken, ttl);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(TokenHashUtil.sha256(request.refreshToken()))
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid."));
        refreshToken.setRevoked(Boolean.TRUE);
        refreshTokenRepository.save(refreshToken);
    }

    private AuthUserPrincipal buildPrincipal(User user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getCode().name())
                .sorted()
                .toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getCode().name())
                .distinct()
                .sorted()
                .toList();
        return new AuthUserPrincipal(user.getId(), user.getUsername(), user.getPassword(), roles, permissions);
    }

    private RefreshToken getValidRefreshToken(String rawToken) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(TokenHashUtil.sha256(rawToken))
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid."));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new InvalidTokenException("Refresh token has been revoked.");
        }
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(Boolean.TRUE);
            refreshTokenRepository.save(refreshToken);
            throw new InvalidTokenException("Refresh token has expired.");
        }
        if (!Boolean.TRUE.equals(refreshToken.getUser().getIsActive())) {
            throw new InvalidTokenException("User account is inactive.");
        }
        return refreshToken;
    }

    private Role defaultCustomerRole() {
        return roleRepository.findByCode(RoleCode.CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException("Default role not found: " + RoleCode.CUSTOMER.name()));
    }

    private String createRefreshToken(User user) {
        String rawToken = generateSecureToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(TokenHashUtil.sha256(rawToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plus(jwtProperties.refreshExpiration()));
        refreshToken.setRevoked(Boolean.FALSE);
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    private AuthResponse authResponse(String accessToken, String refreshToken) {
        return new AuthResponse(accessToken, refreshToken, TOKEN_TYPE, jwtService.getAccessTokenExpiration().toSeconds());
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    private String extractBearerToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith(BEARER_PREFIX)) {
            throw new InvalidTokenException("Bearer access token is required.");
        }
        return bearerToken.substring(BEARER_PREFIX.length());
    }
}
