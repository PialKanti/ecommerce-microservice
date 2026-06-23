package com.example.ecommerce.auth.domain;

import lombok.Builder;

import java.util.List;

/**
 * Application principal derived from a validated user entity.
 * Used for JWT generation. No Spring Security dependency.
 */
@Builder
public record AuthUserPrincipal(
        Long id,
        String username,
        String password,
        List<String> roles,
        List<String> permissions
) {
}
