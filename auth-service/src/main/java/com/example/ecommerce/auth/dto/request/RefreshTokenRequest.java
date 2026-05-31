package com.example.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Payload carrying a refresh token")
public record RefreshTokenRequest(

        @Schema(description = "Opaque refresh token returned by login or a previous refresh call", example = "raw-refresh-token")
        @NotBlank(message = "Refresh token is required")
        String refreshToken

) {
}
