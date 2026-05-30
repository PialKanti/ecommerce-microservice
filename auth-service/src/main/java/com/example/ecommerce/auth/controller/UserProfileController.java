package com.example.ecommerce.auth.controller;

import com.example.ecommerce.auth.dto.request.ChangePasswordRequest;
import com.example.ecommerce.auth.dto.request.UpdateProfileRequest;
import com.example.ecommerce.auth.dto.response.UserResponse;
import com.example.ecommerce.auth.service.UserProfileService;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.commons.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.User.BASE_USERS)
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Authenticated user's own profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(
            summary = "Get current user profile",
            description = "Returns the authenticated user's full profile. The X-User-Id header is forwarded by the API Gateway."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @Parameter(description = "Authenticated user's ID, forwarded by the API Gateway.", required = true)
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully",
                        userProfileService.getProfile(userId)));
    }

    @Operation(
            summary = "Update current user profile",
            description = "Updates the authenticated user's editable profile fields.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UpdateProfileRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "firstName": "Pial",
                                      "lastName": "Samadder",
                                      "phoneNumber": "+8801700000000"
                                    }
                                    """)
                    )
            )
    )
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Parameter(description = "Authenticated user's ID, forwarded by the API Gateway.", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully",
                        userProfileService.updateProfile(userId, request)));
    }

    @Operation(
            summary = "Change current user password",
            description = "Changes the authenticated user's password after verifying the current password.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ChangePasswordRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "currentPassword": "OldPass123",
                                      "newPassword": "NewPass456",
                                      "confirmPassword": "NewPass456"
                                    }
                                    """)
                    )
            )
    )
    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "Authenticated user's ID, forwarded by the API Gateway.", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}
