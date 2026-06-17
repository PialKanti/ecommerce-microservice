package com.example.ecommerce.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
@Schema(description = "Permission details returned in API responses")
public record PermissionResponse(

        @Schema(description = "Unique permission ID", example = "1")
        Long id,

        @Schema(description = "Human-readable permission name", example = "Create Product")
        String name,

        @Schema(description = "Enum code identifying this permission", example = "PERMISSION_PRODUCT_CREATE")
        String code,

        @Schema(description = "Explanation of what this permission grants", example = "Allows creation of product catalog records.")
        String description,

        @Schema(description = "Timestamp when the permission was created", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Timestamp of the last update", example = "2024-06-20T14:45:00")
        LocalDateTime modifiedAt,

        @Schema(description = "ID of the user who created this permission", example = "1")
        Long createdBy,

        @Schema(description = "ID of the user who last modified this permission", example = "1")
        Long modifiedBy

) {
}
