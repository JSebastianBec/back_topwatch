package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.dto.AuthResponse;
import com.topwatch.back_topwatch.dto.LoginRequest;
import com.topwatch.back_topwatch.dto.LogoutRequest;
import com.topwatch.back_topwatch.dto.RegisterRequest;
import com.topwatch.back_topwatch.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register user", description = "Creates a new account and returns access token and refresh token")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Login", description = "Authenticates the user and returns access token and refresh token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Refresh tokens",
            description = "Receives the refresh token in the Authorization header and returns new tokens",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.refreshToken(authHeader.substring(7)));
    }

    @Operation(
            summary = "Logout",
            description = "Receives the access token in the Authorization header and, optionally, the refresh " +
                    "token in the body, and revokes both so they can no longer be used",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody(required = false) LogoutRequest request
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String refreshToken = request != null ? request.refreshToken() : null;
        authService.logout(authHeader.substring(7), refreshToken);
        return ResponseEntity.noContent().build();
    }
}
