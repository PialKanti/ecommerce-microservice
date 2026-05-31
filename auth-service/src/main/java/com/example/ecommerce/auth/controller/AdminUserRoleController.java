package com.example.ecommerce.auth.controller;

import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.auth.dto.response.RoleResponse;
import com.example.ecommerce.auth.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_USERS)
@RequiredArgsConstructor
@Tag(
        name = "User Role Assignment",
        description = "Assign and remove RBAC roles from user accounts. " +
                "Requires `PERMISSION_ROLE_ASSIGN`, enforced by the api-gateway."
)
@SecurityRequirement(name = "bearerAuth")
public class AdminUserRoleController {

    private final UserRoleService userRoleService;

    @Operation(
            summary = "List user roles",
            description = "Returns all roles currently assigned to the specified user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Role list returned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No user with the given ID", content = @Content)
    })
    @GetMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getUserRoles(
            @Parameter(description = "Numeric ID of the user", example = "1", required = true)
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userRoleService.getRoles(userId)));
    }

    @Operation(
            summary = "Assign role to user",
            description = "Assigns an existing role to the specified user. " +
                    "The updated list of assigned roles is returned."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Role assigned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "User or role not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "Role is already assigned to this user", content = @Content)
    })
    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> assignRole(
            @Parameter(description = "Numeric ID of the user", example = "1", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Numeric ID of the role to assign", example = "2", required = true)
            @PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.success(userRoleService.assignRole(userId, roleId)));
    }

    @Operation(
            summary = "Remove role from user",
            description = "Removes an assigned role from the specified user. " +
                    "The updated list of remaining roles is returned."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Role removed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "User or role not found", content = @Content)
    })
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> removeRole(
            @Parameter(description = "Numeric ID of the user", example = "1", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Numeric ID of the role to remove", example = "2", required = true)
            @PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.success(userRoleService.removeRole(userId, roleId)));
    }
}
