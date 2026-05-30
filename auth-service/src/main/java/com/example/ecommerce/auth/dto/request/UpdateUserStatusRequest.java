package com.example.ecommerce.auth.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "isActive is required")
        Boolean isActive
) {
}
