package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.enums.Role;
import com.topwatch.back_topwatch.dto.UpdateUserRequest;
import com.topwatch.back_topwatch.service.ImageStorageService;
import com.topwatch.back_topwatch.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;
    private final ImageStorageService imageStorageService;

    @Operation(
            summary = "Health check",
            description = "Returns a simple health check message"
    )
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Hello world");
    }

    @Operation(
            summary = "Get current user",
            description = "Returns the authenticated user's profile",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(currentUser);
    }

    @Operation(
            summary = "Update current user",
            description = "Updates the authenticated user's profile with the provided fields",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/me/update")
    public ResponseEntity<User> update(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(userService.update(currentUser, request));
    }

    @Operation(
            summary = "Delete user",
            description = "Deletes the user with the given id and their personal data. Watch lists and items other users "
                    + "depend on are kept without any personal data. Allowed only for the user themselves or an ADMIN; anyone else gets 403.",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> delete(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        boolean isSelf = id.equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isSelf && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Optional<User> user = userService.findUserById(id);
        if (user.isPresent()){
            // The account is already gone, so a storage failure must not turn this into an error
            for (String imageUrl : userService.delete(user.get())) {
                try {
                    imageStorageService.deleteIfOwned(imageUrl, id);
                } catch (RuntimeException e) {
                    log.warn("Could not delete image of user {}: {}", id, e.getMessage());
                }
            }
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
