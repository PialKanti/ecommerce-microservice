package com.example.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for changing the authenticated user's password")
public record ChangePasswordRequest(

        @Schema(description = "The user's current password", example = "OldPass123")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "Desired new password (8–100 characters)", example = "NewPass456", minLength = 8, maxLength = 100)
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
        String newPassword,

        @Schema(description = "Must match newPassword exactly", example = "NewPass456")
        @NotBlank(message = "Confirm password is required")
        String confirmPassword

) {
}
