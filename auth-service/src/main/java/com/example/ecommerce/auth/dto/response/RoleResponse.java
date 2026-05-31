package com.example.ecommerce.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Role details returned in API responses")
public record RoleResponse(

        @Schema(description = "Unique role ID", example = "1")
        Long id,

        @Schema(description = "Human-readable role name", example = "Support Agent")
        String name,

        @Schema(description = "Enum code identifying this role", example = "SUPPORT_AGENT")
        String code,

        @Schema(description = "Explanation of this role's responsibilities", example = "Can read and cancel customer orders.")
        String description,

        @Schema(description = "Set of permission codes granted by this role", example = "[\"PERMISSION_ORDER_READ\", \"PERMISSION_ORDER_CANCEL\"]")
        Set<String> permissions,

        @Schema(description = "Timestamp when the role was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of the last update", example = "2024-06-20T14:45:00")
        LocalDateTime modifiedAt,

        @Schema(description = "ID of the user who created this role", example = "1")
        Long createdBy,

        @Schema(description = "ID of the user who last modified this role", example = "1")
        Long modifiedBy

) {
}
