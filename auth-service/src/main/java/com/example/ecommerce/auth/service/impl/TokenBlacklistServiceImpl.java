package com.example.ecommerce.auth.service.impl;

import com.example.ecommerce.auth.service.TokenBlacklistService;
import com.example.ecommerce.auth.util.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "auth:blacklist:";
    private static final String BLACKLIST_VALUE      = "revoked";

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void blacklist(String token, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }
        stringRedisTemplate.opsForValue().set(buildKey(token), BLACKLIST_VALUE, ttl);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(token)));
    }

    private String buildKey(String token) {
        return BLACKLIST_KEY_PREFIX + TokenHashUtil.sha256(token);
    }
}
