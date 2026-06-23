package com.example.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for updating the authenticated user's editable profile fields")
public record UpdateProfileRequest(

        @Schema(description = "Updated given name", example = "Pial", maxLength = 100)
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @Schema(description = "Updated family name", example = "Samadder", maxLength = 100)
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @Schema(description = "Updated contact phone number in E.164 format", example = "+8801700000000", maxLength = 30)
        @Size(max = 30, message = "Phone number cannot exceed 30 characters")
        String phoneNumber

) {
}
