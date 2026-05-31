package com.example.ecommerce.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token pair returned after a successful login or token refresh")
public record AuthResponse(

        @Schema(description = "Short-lived JWT access token — include as `Authorization: Bearer <token>`", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Long-lived opaque refresh token — store securely and use to rotate the token pair", example = "d3f1a8b2c4e5f6a7b8c9d0e1f2a3b4c5")
        String refreshToken,

        @Schema(description = "Token type prefix used in the Authorization header", example = "Bearer")
        String tokenType,

        @Schema(description = "Access token validity period in seconds", example = "900")
        Long expiresIn

) {
}
