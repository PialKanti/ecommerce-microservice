package com.example.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating a new user account")
public record RegisterRequest(

        @Schema(description = "User's given name", example = "Pial", maxLength = 100)
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @Schema(description = "User's family name", example = "Samadder", maxLength = 100)
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @Schema(description = "Unique login handle (3–100 characters)", example = "pial", minLength = 3, maxLength = 100)
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
        String username,

        @Schema(description = "Unique email address", example = "pial@example.com", maxLength = 150)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @Schema(description = "Contact phone number in E.164 format", example = "+8801700000000", maxLength = 30)
        @Size(max = 30, message = "Phone number cannot exceed 30 characters")
        String phoneNumber,

        @Schema(description = "Account password (8–100 characters)", example = "StrongPass123", minLength = 8, maxLength = 100)
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password

) {
}
