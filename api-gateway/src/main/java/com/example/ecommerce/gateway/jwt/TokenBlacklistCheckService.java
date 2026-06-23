package com.example.ecommerce.gateway.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class TokenBlacklistCheckService {

    private static final String KEY_PREFIX        = "auth:blacklist:";
    private static final String USER_BLOCK_PREFIX = "auth:user:blocked:";

    private final StringRedisTemplate redisTemplate;

    public boolean isBlacklisted(String rawToken) {
        return hasKey(KEY_PREFIX + sha256(rawToken));
    }

    public boolean isUserBlocked(Long userId) {
        return hasKey(USER_BLOCK_PREFIX + userId);
    }

    private boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
