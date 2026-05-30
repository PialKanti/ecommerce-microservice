package com.example.ecommerce.auth.controller;

import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.auth.dto.request.RoleRequest;
import com.example.ecommerce.auth.dto.response.PermissionResponse;
import com.example.ecommerce.auth.dto.response.RoleResponse;
import com.example.ecommerce.auth.service.RoleService;
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

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_ROLES)
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "Administrative RBAC role operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminRoleController {

    private final RoleService roleService;

    @Operation(summary = "List roles", description = "Returns a paginated list of all RBAC roles.")
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<RoleResponse>>> listRoles(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(roleService.getAll(pageable))));
    }

    @Operation(summary = "Get role by ID", description = "Returns a single role by its numeric ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getById(id)));
    }

    @Operation(
            summary = "Create role",
            description = "Creates a new RBAC role entry.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RoleRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "name": "Support Agent",
                                      "code": "SUPPORT_AGENT",
                                      "description": "Can read and cancel customer orders."
                                    }
                                    """)
                    )
            )
    )
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.create(request)));
    }

    @Operation(summary = "Update role", description = "Updates an existing role by its numeric ID.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.update(id, request)));
    }

    @Operation(summary = "Delete role", description = "Permanently removes a role by its numeric ID.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List role permissions", description = "Returns all permissions assigned to a role.")
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions(@PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getPermissions(roleId)));
    }

    @Operation(summary = "Assign permission to role", description = "Assigns an existing permission to a role by their numeric IDs.")
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(ApiResponse.success(roleService.assignPermission(roleId, permissionId)));
    }

    @Operation(summary = "Remove permission from role", description = "Removes an assigned permission from a role by their numeric IDs.")
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermission(
            @PathVariable Long roleId,
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(ApiResponse.success(roleService.removePermission(roleId, permissionId)));
    }
}
