package com.example.ecommerce.auth.controller;

import com.example.ecommerce.commons.dto.response.ApiResponse;
import com.example.ecommerce.commons.constants.ApiEndpoints;
import com.example.ecommerce.auth.dto.request.LoginRequest;
import com.example.ecommerce.auth.dto.request.RefreshTokenRequest;
import com.example.ecommerce.auth.dto.request.RegisterRequest;
import com.example.ecommerce.auth.dto.response.AuthResponse;
import com.example.ecommerce.auth.dto.response.UserResponse;
import com.example.ecommerce.auth.service.AuthService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiEndpoints.Auth.BASE_AUTH)
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "Public endpoints for registration, login, and token refresh. " +
                "The logout endpoint requires a valid Bearer token."
)
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Register user",
            description = "Creates a new active user account with a BCrypt-encrypted password.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "User registration payload containing identity and credential details.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegisterRequest.class),
                            examples = @ExampleObject(
                                    name = "Register user",
                                    value = """
                                            {
                                              "firstName": "Pial",
                                              "lastName": "Samadder",
                                              "username": "pial",
                                              "email": "pial@example.com",
                                              "phoneNumber": "+8801700000000",
                                              "password": "StrongPass123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "User registered successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "Invalid registration payload — check field constraints", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "Username or email already exists", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", authService.register(request)));
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates a user by username and password, then issues a new access and refresh token pair.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Login payload containing username and password.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login user",
                                    value = """
                                            {
                                              "username": "pial",
                                              "password": "StrongPass123"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "User authenticated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Invalid username or password", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }

    @Operation(
            summary = "Refresh token",
            description = "Validates a persisted refresh token, revokes it, and returns a newly rotated access and refresh token pair.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Payload containing the refresh token to rotate.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RefreshTokenRequest.class),
                            examples = @ExampleObject(
                                    name = "Refresh token",
                                    value = """
                                            {
                                              "refreshToken": "raw-refresh-token"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Token pair rotated successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Refresh token is invalid, expired, or already revoked", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", authService.refresh(request)));
    }

    @Operation(
            summary = "Logout user",
            description = "Revokes the submitted refresh token and blacklists the active access token in Redis. " +
                    "The access token will be rejected by the api-gateway for its remaining lifetime.",
            security = @SecurityRequirement(name = "bearerAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Payload containing the refresh token to revoke.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RefreshTokenRequest.class),
                            examples = @ExampleObject(
                                    name = "Logout user",
                                    value = """
                                            {
                                              "refreshToken": "raw-refresh-token"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "Logout successful — access token blacklisted, refresh token revoked",
                    content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                    description = "Access token or refresh token is invalid", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true)
            @RequestHeader(name = "Authorization") String authorization,
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(authorization, request);
        return ResponseEntity.ok(ApiResponse.<Void>success("Logout successful"));
    }
}
