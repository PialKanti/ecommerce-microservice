package com.example.ecommerce.auth.dto.request;

import com.example.ecommerce.commons.enums.PermissionCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PermissionRequest(
        @NotBlank(message = "Permission name is required")
        @Size(max = 120, message = "Permission name cannot exceed 120 characters")
        String name,

        @NotNull(message = "Permission code is required")
        PermissionCode code,

        @Size(max = 500, message = "Permission description cannot exceed 500 characters")
        String description
) {
}
