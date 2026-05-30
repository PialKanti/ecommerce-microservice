package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.dto.request.UpdateUserStatusRequest;
import com.example.ecommerce.auth.dto.response.UserResponse;
import com.example.ecommerce.auth.service.AdminUserService;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.dto.response.PaginatedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Admin.BASE_ADMIN_USERS)
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Administrative user management operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(
            summary = "List users",
            description = "Returns a paginated list of users. Optionally filter by search term (matches username, email, first name, last name) and/or active status."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                PaginatedResponse.of(adminUserService.getAll(search, isActive, pageable))));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Returns a single user by their numeric ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getById(id)));
    }

    @Operation(
            summary = "Update user status",
            description = "Activates or deactivates a user account. This operation is idempotent."
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminUserId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "User status updated successfully",
                adminUserService.updateStatus(id, request, adminUserId)));
    }
}
