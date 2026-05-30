package com.example.ecommerce.auth.controller;

import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.auth.dto.request.PermissionRequest;
import com.example.ecommerce.auth.dto.response.PermissionResponse;
import com.example.ecommerce.auth.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Permission Management", description = "Administrative RBAC permission operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminPermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "List permissions", description = "Returns a paginated list of all RBAC permissions.")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<PermissionResponse>>> listPermissions(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(permissionService.getAll(pageable))));
    }

    @Operation(summary = "Get permission by ID", description = "Returns a single permission by its numeric ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermission(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.getById(id)));
    }

    @Operation(
            summary = "Create permission",
            description = "Creates a new RBAC permission entry.",
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
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.create(request)));
    }

    @Operation(summary = "Update permission", description = "Updates an existing permission by its numeric ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(permissionService.update(id, request)));
    }

    @Operation(summary = "Delete permission", description = "Permanently removes a permission by its numeric ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
