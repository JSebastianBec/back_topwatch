package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchListView;
import com.topwatch.back_topwatch.dto.RecentlyWatchedItemResponse;
import com.topwatch.back_topwatch.dto.StartWatchListViewRequest;
import com.topwatch.back_topwatch.dto.UpdateWatchedRequest;
import com.topwatch.back_topwatch.dto.WatchListViewDetailResponse;
import com.topwatch.back_topwatch.dto.WatchListViewResponse;
import com.topwatch.back_topwatch.service.WatchListViewService;
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
@RequestMapping("/api/v1/watch-list-views")
@RequiredArgsConstructor
@Tag(name = "Watch List Views", description = "Start viewing a watch list and track your own progress through it")
public class WatchListViewController {

    private final WatchListViewService watchListViewService;

    @Operation(
            summary = "Start viewing a watch list",
            description = "Starts a view of any watch list (your own or someone else's). The watch list itself is not modified",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping
    public ResponseEntity<WatchListViewResponse> startView(
            @Valid @RequestBody StartWatchListViewRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        WatchListView view = watchListViewService.startView(request.watchListId(), currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(view));
    }

    @Operation(
            summary = "Get my watch list views",
            description = "Returns every watch list the authenticated user is viewing, each with its progress",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping
    public List<WatchListViewResponse> getMyViews(@AuthenticationPrincipal User currentUser) {
        return watchListViewService.findViewsByViewer(currentUser).stream()
                .map(this::toResponse)
                .toList();
    }

    @Operation(
            summary = "Get my recently watched items",
            description = "Returns the items the authenticated user marked as watched across all their watch list views, "
                    + "most recently watched first, each with the watch list it belongs to. Use limit to cap the result (default 20, max 100)",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/recently-watched")
    public List<RecentlyWatchedItemResponse> getRecentlyWatched(
            @RequestParam(defaultValue = "" + WatchListViewService.DEFAULT_RECENTLY_WATCHED_LIMIT) int limit,
            @AuthenticationPrincipal User currentUser
    ) {
        return watchListViewService.findRecentlyWatchedItems(currentUser, limit).stream()
                .map(RecentlyWatchedItemResponse::from)
                .toList();
    }

    @Operation(
            summary = "Get watch list view detail",
            description = "Returns the view with its progress and every item of the watch list with your watched state",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/{id}")
    public WatchListViewDetailResponse getViewDetail(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        return toDetailResponse(watchListViewService.findOwnView(id, currentUser));
    }

    @Operation(
            summary = "Mark item as watched or unwatched",
            description = "Sets your watched state for one entry of the viewed watch list, identified by its watch list item id "
                    + "(the watchListItemId / id field of each entry in the view and list detail responses), and returns the updated view",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/{id}/items/{watchListItemId}")
    public WatchListViewDetailResponse setWatched(
            @PathVariable Long id,
            @PathVariable Long watchListItemId,
            @Valid @RequestBody UpdateWatchedRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return toDetailResponse(watchListViewService.setWatched(id, watchListItemId, request.watched(), currentUser));
    }

    @Operation(
            summary = "Stop viewing a watch list",
            description = "Removes your view and all the watched state stored in it. The watch list itself is not modified "
                    + "and you can start viewing it again later. To unmark a single item use POST /{id}/items/{watchListItemId} with watched = false",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/{id}/delete")
    public ResponseEntity<Void> deleteView(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        watchListViewService.deleteView(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    private WatchListViewResponse toResponse(WatchListView view) {
        return WatchListViewResponse.from(
                view,
                watchListViewService.countWatchedItems(view),
                watchListViewService.countTotalItems(view)
        );
    }

    private WatchListViewDetailResponse toDetailResponse(WatchListView view) {
        return WatchListViewDetailResponse.from(
                view,
                watchListViewService.findWatchListItems(view),
                watchListViewService.findViewItemsByWatchListItemId(view)
        );
    }

}
