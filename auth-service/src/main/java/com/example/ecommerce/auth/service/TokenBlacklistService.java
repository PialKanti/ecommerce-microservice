package com.example.ecommerce.auth.service;

import java.time.Duration;

public interface TokenBlacklistService {
    void blacklist(String token, Duration ttl);

    void blockUser(Long userId, Duration ttl);

    void unblockUser(Long userId);
}
