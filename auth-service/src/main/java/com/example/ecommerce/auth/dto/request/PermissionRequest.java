package com.example.ecommerce.auth.dto.request;

import com.example.ecommerce.commons.enums.PermissionCode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload for creating or updating an RBAC permission")
public record PermissionRequest(

        @Schema(description = "Human-readable permission name", example = "Create Product", maxLength = 120)
        @NotBlank(message = "Permission name is required")
        @Size(max = 120, message = "Permission name cannot exceed 120 characters")
        String name,

        @Schema(description = "Enum code identifying this permission", example = "PERMISSION_PRODUCT_CREATE")
        @NotNull(message = "Permission code is required")
        PermissionCode code,

        @Schema(description = "Optional explanation of what this permission grants", example = "Allows creation of product catalog records.", maxLength = 500)
        @Size(max = 500, message = "Permission description cannot exceed 500 characters")
        String description

) {
}
