package com.example.ecommerce.auth.controller;

import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.auth.dto.response.RoleResponse;
import com.example.ecommerce.auth.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "User Role Assignment", description = "Administrative user-role assignment operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserRoleController {

    private final UserRoleService userRoleService;

    @Operation(summary = "List user roles")
    @GetMapping("/{userId}/roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getUserRoles(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userRoleService.getRoles(userId)));
    }

    @Operation(summary = "Assign role to user")
    @PostMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> assignRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.success(userRoleService.assignRole(userId, roleId)));
    }

    @Operation(summary = "Remove role from user")
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {
        return ResponseEntity.ok(ApiResponse.success(userRoleService.removeRole(userId, roleId)));
    }
}
