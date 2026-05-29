package com.example.ecommerce.auth.dto.request;

import com.example.ecommerce.commons.enums.RoleCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoleRequest(
        @NotBlank(message = "Role name is required")
        @Size(max = 120, message = "Role name cannot exceed 120 characters")
        String name,

        @NotNull(message = "Role code is required")
        RoleCode code,

        @Size(max = 500, message = "Role description cannot exceed 500 characters")
        String description
) {
}
