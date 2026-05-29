package com.example.ecommerce.gateway.jwt;

import com.example.ecommerce.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class GatewayJwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String USER_ID_CLAIM = "userId";

    private final JwtProperties jwtProperties;

    public boolean isTokenValid(String token) {
        try {
            return !extractClaim(token, Claims::getExpiration).before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public Long extractUserId(String token) {
        try {
            Object raw = extractAllClaims(token).get(USER_ID_CLAIM);
            if (raw instanceof Number n) return n.longValue();
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            Object raw = extractAllClaims(token).get(ROLES_CLAIM);
            return raw instanceof List<?> list ? (List<String>) list : List.of();
        } catch (JwtException | IllegalArgumentException e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        try {
            Object raw = extractAllClaims(token).get(PERMISSIONS_CLAIM);
            return raw instanceof List<?> list ? (List<String>) list : List.of();
        } catch (JwtException | IllegalArgumentException e) {
            return List.of();
        }
    }

    public Duration getRemainingLifetime(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        long remainingMillis = expiration.toInstant().toEpochMilli() - Instant.now().toEpochMilli();
        return Duration.ofMillis(Math.max(remainingMillis, 0));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.signingKey().getBytes(StandardCharsets.UTF_8));
    }
}
