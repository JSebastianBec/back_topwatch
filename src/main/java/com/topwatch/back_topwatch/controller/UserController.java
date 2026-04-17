package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.dto.UpdateUserRequest;
import com.topwatch.back_topwatch.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Health check",
            description = "Returns a simple health check message"
    )
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Hello world");
    }

    @Operation(
            summary = "Update current user",
            description = "Updates the authenticated user's profile with the provided fields",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/me")
    public ResponseEntity<User> update(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(userService.update(currentUser, request));
    }

    @Operation(
            summary = "Delete current user",
            description = "Delete the authenticated user's profile with the provided fields",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> delete(
            @PathVariable("id") Long id
    ) {
        Optional<User> user = userService.findUserById(id);
        if (user.isPresent()){
            userService.delete(user.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Return all users",
            description = "Return a list of all register users",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    @Operation(
            summary = "Get given user",
            description = "Get the user given an id",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/user/{id}")
    public ResponseEntity<User> getUser(
            @PathVariable("id") Long id
    ) {
        Optional<User> user = userService.findUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
