package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.WatchList;
import com.topwatch.back_topwatch.domain.WatchListView;
import com.topwatch.back_topwatch.dto.AddToMyWatchListsRequest;
import com.topwatch.back_topwatch.dto.AddWatchListItemsRequest;
import com.topwatch.back_topwatch.dto.RemoveWatchListItemsRequest;
import com.topwatch.back_topwatch.dto.CreateWatchListRequest;
import com.topwatch.back_topwatch.dto.WatchListDetailResponse;
import com.topwatch.back_topwatch.dto.WatchListResponse;
import com.topwatch.back_topwatch.service.WatchListService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/watch-lists")
@RequiredArgsConstructor
@Tag(name = "Watch Lists", description = "Watch list endpoints")
public class WatchListController {

    private final WatchListService watchListService;
    private final WatchListViewService watchListViewService;

    @Operation(
            summary = "Get all watch lists",
            description = "Each watch list includes started/viewId telling whether the authenticated user already started watching it",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping
    public List<WatchListResponse> getAllWatchLists(@AuthenticationPrincipal User currentUser) {
        Map<Long, Long> viewIds = watchListViewService.findViewIdsByWatchListId(currentUser);
        return watchListService.findAllWatchLists().stream()
                .map(watchList -> WatchListResponse.from(watchList, viewIds.get(watchList.getId()), currentUser.getId()))
                .toList();
    }

    @Operation(
            summary = "Get my watch lists",
            description = "Returns the watch lists created by the authenticated user followed by the ones from other users they added. Use creator to tell them apart",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/me")
    public List<WatchListResponse> getMyWatchLists(@AuthenticationPrincipal User currentUser) {
        Map<Long, Long> viewIds = watchListViewService.findViewIdsByWatchListId(currentUser);
        return watchListService.findMyWatchLists(currentUser).stream()
                .map(watchList -> WatchListResponse.from(watchList, viewIds.get(watchList.getId()), currentUser.getId()))
                .toList();
    }

    @Operation(
            summary = "Get watch lists to add",
            description = "Returns the watch lists from other users that the authenticated user has not added to their lists nor started watching yet",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/not-started")
    public List<WatchListResponse> getNotStartedWatchLists(@AuthenticationPrincipal User currentUser) {
        return watchListService.findWatchListsNotStartedBy(currentUser).stream()
                .map(watchList -> WatchListResponse.from(watchList, null, currentUser.getId()))
                .toList();
    }

    @Operation(
            summary = "Add other users' watch lists to mine",
            description = "Adds one or more watch lists created by other users to the authenticated user's lists (GET /me). They stay read-only: only their creator can change them. All are added or none are",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/me/add")
    public ResponseEntity<List<WatchListResponse>> addToMyWatchLists(
            @Valid @RequestBody AddToMyWatchListsRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchListService.addToMyWatchLists(request.watchListIds(), currentUser).stream()
                        .map(watchList -> WatchListResponse.from(watchList, null, currentUser.getId()))
                        .toList());
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

        return ResponseEntity.status(HttpStatus.CREATED).body(WatchListResponse.from(watchListService.saveWatchList(watchList), null, currentUser.getId()));
    }

    @Operation(
            summary = "Get watch list detail",
            description = "Returns the watch list plus the items associated with it, and whether the authenticated user already started watching it",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/{id}")
    public ResponseEntity<WatchListDetailResponse> getWatchListDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return watchListService.findWatchListById(id)
                .map(watchList -> WatchListDetailResponse.from(
                        watchList,
                        watchListService.findItemsByWatchListId(id),
                        watchListViewService.findViewByViewerAndWatchList(currentUser, id)
                                .map(WatchListView::getId)
                                .orElse(null),
                        currentUser.getId()))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Add items to watch list",
            description = "Links one or more existing items (by id) to a watch list owned by the authenticated user. All items are added or none are",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/{id}/items")
    public ResponseEntity<List<WatchListDetailResponse.WatchListItemEntry>> addItemsToWatchList(
            @PathVariable Long id,
            @Valid @RequestBody AddWatchListItemsRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(watchListService.addItemsToWatchList(id, request.itemIds(), currentUser).stream()
                        .map(WatchListDetailResponse.WatchListItemEntry::from)
                        .toList());
    }

    @Operation(
            summary = "Remove items from watch list",
            description = "Removes one or more items (by item id) from a watch list owned by the authenticated user, together with "
                    + "every viewer's watched state for them. The items are kept. All items are removed or none are",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/{id}/items/remove")
    public ResponseEntity<Void> removeItemsFromWatchList(
            @PathVariable Long id,
            @Valid @RequestBody RemoveWatchListItemsRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        watchListService.removeItemsFromWatchList(id, request.itemIds(), currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete watch list",
            description = "Deletes a watch list created by the authenticated user, together with its entries, every user's view of it "
                    + "and every user's link to it in their lists. The items are kept. Only the creator can delete it",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/{id}/delete")
    public ResponseEntity<Void> deleteWatchList(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        watchListService.deleteWatchList(id, currentUser);
        return ResponseEntity.noContent().build();
    }

}
