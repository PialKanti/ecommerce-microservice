package com.example.ecommerce.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Schema(description = "User account details returned in API responses")
public record UserResponse(

        @Schema(description = "Unique user ID", example = "1")
        Long id,

        @Schema(description = "User's given name", example = "Pial")
        String firstName,

        @Schema(description = "User's family name", example = "Samadder")
        String lastName,

        @Schema(description = "Unique login handle", example = "pial")
        String username,

        @Schema(description = "User's email address", example = "pial@example.com")
        String email,

        @Schema(description = "Contact phone number", example = "+8801700000000")
        String phoneNumber,

        @Schema(description = "Whether the account is active and can log in", example = "true")
        Boolean isActive,

        @Schema(description = "Set of role codes currently assigned to this user", example = "[\"CUSTOMER\"]")
        Set<String> roles,

        @Schema(description = "Timestamp when the account was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of the last update", example = "2024-06-20T14:45:00")
        LocalDateTime modifiedAt,

        @Schema(description = "ID of the user who created this account", example = "1")
        Long createdBy,

        @Schema(description = "ID of the user who last modified this account", example = "1")
        Long modifiedBy

) {
}
