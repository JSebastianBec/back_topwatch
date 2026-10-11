package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.dto.CreateWatchListRequest;
import com.topwatch.back_topwatch.dto.WatchListDetailResponse;
import com.topwatch.back_topwatch.dto.WatchListResponse;
import com.topwatch.back_topwatch.service.WatchListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/watch-lists")
@RequiredArgsConstructor
@Tag(name = "Watch Lists", description = "Watch list endpoints")
public class WatchListController {

    private final WatchListService watchListService;

    @Operation(
            summary = "Get all watch lists",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping
    public List<WatchListResponse> getAllWatchLists() {
        return watchListService.findAllWatchLists().stream()
                .map(WatchListResponse::from)
                .toList();
    }

    @Operation(
            summary = "Create watch list",
            description = "Creates a new watch list owned by the authenticated user",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/save")
    public ResponseEntity<WatchListResponse> createWatchList(
            @Valid @RequestBody CreateWatchListRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        WatchList watchList = WatchList.builder()
                .name(request.name())
                .description(request.description())
                .creator(currentUser)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(WatchListResponse.from(watchListService.saveWatchList(watchList)));
    }

    @Operation(
            summary = "Get watch list detail",
            description = "Returns the watch list plus the items associated with it",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/{id}")
    public ResponseEntity<WatchListDetailResponse> getWatchListDetail(@PathVariable Long id) {
        return watchListService.findWatchListById(id)
                .map(watchList -> WatchListDetailResponse.from(watchList, watchListService.findItemsByWatchListId(id)))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
