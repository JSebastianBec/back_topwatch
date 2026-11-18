package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.dto.BlockedUserResponse;
import com.topwatch.back_topwatch.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/blocks")
@RequiredArgsConstructor
@Tag(name = "Blocks", description = "Users the authenticated user blocked")
public class BlockController {

    private final BlockService blockService;

    @Operation(summary = "List blocked users", security = @SecurityRequirement(name = "Bearer Token"))
    @GetMapping
    public List<BlockedUserResponse> getBlockedUsers(@AuthenticationPrincipal User currentUser) {
        return blockService.findBlockedUsers(currentUser);
    }

    @Operation(
            summary = "Block user",
            description = "Hides the watch lists and items created by that user from the authenticated user",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/{userId}")
    public ResponseEntity<Void> blockUser(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        blockService.block(userId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Unblock user", security = @SecurityRequirement(name = "Bearer Token"))
    @PostMapping("/{userId}/remove")
    public ResponseEntity<Void> unblockUser(@PathVariable Long userId, @AuthenticationPrincipal User currentUser) {
        blockService.unblock(userId, currentUser);
        return ResponseEntity.noContent().build();
    }

}
