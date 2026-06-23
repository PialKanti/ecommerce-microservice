package com.example.ecommerce.auth.controller;

import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.auth.dto.request.PermissionRequest;
import com.example.ecommerce.auth.dto.response.PermissionResponse;
import com.example.ecommerce.auth.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_PERMISSIONS)
@RequiredArgsConstructor
@Tag(
        name = "Permission Management",
        description = "Administrative CRUD for RBAC permissions. " +
                "Requires `PERMISSION_READ` to list/read and `PERMISSION_MANAGE` for write operations, " +
                "enforced by the api-gateway."
)
@SecurityRequirement(name = "bearerAuth")
public class AdminPermissionController {

    private final PermissionService permissionService;

    @Operation(
            summary = "List permissions",
            description = "Returns a paginated list of all RBAC permissions ordered by creation date descending."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Page of permissions returned successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<PermissionResponse>>> listPermissions(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(permissionService.getAll(pageable))));
    }

    @Operation(
            summary = "Get permission by ID",
            description = "Returns a single RBAC permission by its numeric ID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Permission found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PermissionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No permission with the given ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermission(
            @Parameter(description = "Numeric ID of the permission", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.getById(id)));
    }

    @Operation(
            summary = "Create permission",
            description = "Creates a new RBAC permission entry. The `code` must be a valid `PermissionCode` enum value.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PermissionRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Create Product",
                                      "code": "PERMISSION_PRODUCT_CREATE",
                                      "description": "Allows creation of product catalog records."
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Permission created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PermissionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid payload — check field constraints", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "A permission with that code already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.create(request)));
    }

    @Operation(
            summary = "Update permission",
            description = "Replaces all fields of an existing permission. The `code` must be a valid `PermissionCode` enum value.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PermissionRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Create Product",
                                      "code": "PERMISSION_PRODUCT_CREATE",
                                      "description": "Allows creation of product catalog records."
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Permission updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PermissionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid payload — check field constraints", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No permission with the given ID", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @Parameter(description = "Numeric ID of the permission to update", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.update(id, request)));
    }

    @Operation(
            summary = "Delete permission",
            description = "Permanently removes a permission by its numeric ID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204",
                    description = "Permission deleted successfully", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No permission with the given ID", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(
            @Parameter(description = "Numeric ID of the permission to delete", example = "1", required = true)
            @PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
