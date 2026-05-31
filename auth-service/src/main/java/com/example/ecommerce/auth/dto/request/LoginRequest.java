package com.example.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credentials for authenticating a user")
public record LoginRequest(

        @Schema(description = "Registered username", example = "pial")
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "Account password", example = "StrongPass123")
        @NotBlank(message = "Password is required")
        String password

) {
}
