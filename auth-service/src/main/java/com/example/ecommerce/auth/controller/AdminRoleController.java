package com.example.ecommerce.auth.controller;

import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.auth.dto.request.RoleRequest;
import com.example.ecommerce.auth.dto.response.PermissionResponse;
import com.example.ecommerce.auth.dto.response.RoleResponse;
import com.example.ecommerce.auth.service.RoleService;
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

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_ROLES)
@RequiredArgsConstructor
@Tag(
        name = "Role Management",
        description = "Administrative CRUD for RBAC roles and permission assignment. " +
                "Requires `PERMISSION_ROLE_READ` to list/read and `PERMISSION_ROLE_*` permissions " +
                "for write operations, enforced by the api-gateway."
)
@SecurityRequirement(name = "bearerAuth")
public class AdminRoleController {

    private final RoleService roleService;

    @Operation(
            summary = "List roles",
            description = "Returns a paginated list of all RBAC roles ordered by creation date descending."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Page of roles returned successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<RoleResponse>>> listRoles(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(PaginatedResponse.of(roleService.getAll(pageable))));
    }

    @Operation(
            summary = "Get role by ID",
            description = "Returns a single role by its numeric ID, including its assigned permissions."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Role found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RoleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No role with the given ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(
            @Parameter(description = "Numeric ID of the role", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getById(id)));
    }

    @Operation(
            summary = "Create role",
            description = "Creates a new RBAC role entry. The `code` must be a valid `RoleCode` enum value.",
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Role created successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RoleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid payload — check field constraints", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "A role with that code already exists", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.create(request)));
    }

    @Operation(
            summary = "Update role",
            description = "Replaces all fields of an existing role. The `code` must be a valid `RoleCode` enum value.",
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Role updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RoleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid payload — check field constraints", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No role with the given ID", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @Parameter(description = "Numeric ID of the role to update", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(roleService.update(id, request)));
    }

    @Operation(
            summary = "Delete role",
            description = "Permanently removes a role by its numeric ID."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204",
                    description = "Role deleted successfully", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No role with the given ID", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(
            @Parameter(description = "Numeric ID of the role to delete", example = "1", required = true)
            @PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "List role permissions",
            description = "Returns all permissions currently assigned to the specified role."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Permission list returned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No role with the given ID", content = @Content)
    })
    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions(
            @Parameter(description = "Numeric ID of the role", example = "1", required = true)
            @PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.success(roleService.getPermissions(roleId)));
    }

    @Operation(
            summary = "Assign permission to role",
            description = "Assigns an existing permission to a role. Requires `PERMISSION_ROLE_ASSIGN`."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Permission assigned successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RoleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Role or permission not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "Permission is already assigned to this role", content = @Content)
    })
    @PostMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermission(
            @Parameter(description = "Numeric ID of the role", example = "1", required = true)
            @PathVariable Long roleId,
            @Parameter(description = "Numeric ID of the permission to assign", example = "3", required = true)
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(ApiResponse.success(roleService.assignPermission(roleId, permissionId)));
    }

    @Operation(
            summary = "Remove permission from role",
            description = "Removes an assigned permission from a role. Requires `PERMISSION_ROLE_ASSIGN`."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Permission removed successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RoleResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "Role or permission not found", content = @Content)
    })
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermission(
            @Parameter(description = "Numeric ID of the role", example = "1", required = true)
            @PathVariable Long roleId,
            @Parameter(description = "Numeric ID of the permission to remove", example = "3", required = true)
            @PathVariable Long permissionId) {
        return ResponseEntity.ok(ApiResponse.success(roleService.removePermission(roleId, permissionId)));
    }
}
