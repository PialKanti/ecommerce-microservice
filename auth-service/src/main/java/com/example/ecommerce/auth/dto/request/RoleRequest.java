package com.example.ecommerce.auth.dto.request;

import com.example.ecommerce.commons.enums.RoleCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating or updating an RBAC role")
public record RoleRequest(

        @Schema(description = "Human-readable role name", example = "Support Agent", maxLength = 120)
        @NotBlank(message = "Role name is required")
        @Size(max = 120, message = "Role name cannot exceed 120 characters")
        String name,

        @Schema(description = "Enum code identifying this role", example = "SUPPORT_AGENT")
        @NotNull(message = "Role code is required")
        RoleCode code,

        @Schema(description = "Optional explanation of this role's responsibilities", example = "Can read and cancel customer orders.", maxLength = 500)
        @Size(max = 500, message = "Role description cannot exceed 500 characters")
        String description

) {
}
