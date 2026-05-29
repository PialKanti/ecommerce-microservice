package com.example.ecommerce.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String signingKey,
        Duration expiration,
        Duration refreshExpiration
) {
}
