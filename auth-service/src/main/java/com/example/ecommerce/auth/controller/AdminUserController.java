package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.dto.request.UpdateUserStatusRequest;
import com.example.ecommerce.auth.dto.response.UserResponse;
import com.example.ecommerce.auth.service.AdminUserService;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.dto.response.PaginatedResponse;
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
@Tag(
        name = "User Management",
        description = "Administrative operations for reading and managing user accounts. " +
                "Requires `PERMISSION_USER_READ` to list/read and `PERMISSION_USER_MANAGE` " +
                "for status updates, enforced by the api-gateway."
)
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(
            summary = "List users",
            description = "Returns a paginated list of users. " +
                    "Optionally filter by `search` (partial match on username, email, first name, or last name) " +
                    "and/or `isActive` status."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Page of users returned successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> listUsers(
            @Parameter(description = "Filter by username, email, first name, or last name (partial match)", example = "pial")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by account active status", example = "true")
            @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Number of records per page", example = "10")
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(
                PaginatedResponse.of(adminUserService.getAll(search, isActive, pageable))));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Returns a single user by their numeric ID, including assigned roles."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "User found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No user with the given ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @Parameter(description = "Numeric ID of the user", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminUserService.getById(id)));
    }

    @Operation(
            summary = "Update user status",
            description = "Activates or deactivates a user account. Deactivating a user immediately blocks all their " +
                    "requests at the api-gateway (via a Redis block flag). This operation is idempotent. " +
                    "Requires `PERMISSION_USER_MANAGE`.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateUserStatusRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "isActive": false
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "User status updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid payload — `isActive` is required", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "No user with the given ID", content = @Content)
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @Parameter(description = "Numeric ID of the user whose status to update", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminUserId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "User status updated successfully",
                adminUserService.updateStatus(id, request, adminUserId)));
    }
}
