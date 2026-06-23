package com.example.ecommerce.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload for toggling a user account's active state")
public record UpdateUserStatusRequest(

        @Schema(description = "Pass true to activate the account, false to deactivate", example = "false")
        @NotNull(message = "isActive is required")
        Boolean isActive

) {
}
