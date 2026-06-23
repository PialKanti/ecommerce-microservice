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
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "User Profile", description = "Authenticated user's own profile management. Requires a valid Bearer token.")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(
            summary = "Get current user profile",
            description = "Returns the authenticated user's full profile including assigned roles."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Profile retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Missing or invalid Bearer token", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile retrieved successfully",
                        userProfileService.getProfile(userId)));
    }

    @Operation(
            summary = "Update current user profile",
            description = "Updates the authenticated user's editable profile fields (name and phone number).",
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid profile payload — check field constraints", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Missing or invalid Bearer token", content = @Content)
    })
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully",
                        userProfileService.updateProfile(userId, request)));
    }

    @Operation(
            summary = "Change current user password",
            description = "Verifies the current password then replaces it with the new one. " +
                    "`newPassword` and `confirmPassword` must match.",
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204",
                    description = "Password changed successfully", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Validation error or current password is incorrect", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Missing or invalid Bearer token", content = @Content)
    })
    @PostMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @Parameter(hidden = true)
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}
