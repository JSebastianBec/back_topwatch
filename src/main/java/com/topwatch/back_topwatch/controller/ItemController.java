package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.dto.CreateItemRequest;
import com.topwatch.back_topwatch.dto.ItemResponse;
import com.topwatch.back_topwatch.service.ImageStorageService;
import com.topwatch.back_topwatch.service.ItemService;
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
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Item management endpoints")
public class ItemController {

    private final ItemService itemService;
    private final ImageStorageService imageStorageService;

    @Operation(
            summary = "Create item",
            description = "Creates a new item owned by the authenticated user",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/save")
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody CreateItemRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        imageStorageService.validateImageUrl(request.urlAvatar(), currentUser.getId());
        Item item = Item.builder()
                .name(request.name())
                .description(request.description())
                .urlAvatar(request.urlAvatar())
                .type(request.type())
                .creator(currentUser)
                .categories(itemService.resolveCategories(request.categoryIds()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(ItemResponse.from(itemService.saveItem(item)));
    }

    @Operation(
            summary = "Get item by id",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable Long id) {
        Optional<Item> item = itemService.findItemById(id);
        return item.map(ItemResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get all items",
            description = "When excludeWatchListId is provided, items already in that watch list are left out",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping
    public List<ItemResponse> getAllItems(@RequestParam(required = false) Long excludeWatchListId) {
        List<Item> items = excludeWatchListId == null
                ? itemService.findAllItems()
                : itemService.findItemsNotInWatchList(excludeWatchListId);
        return items.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Search items by name",
            description = "Case-insensitive partial match on the item name. When excludeWatchListId is provided, items already in that watch list are left out",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/search")
    public List<ItemResponse> searchItemsByName(
            @RequestParam String name,
            @RequestParam(required = false) Long excludeWatchListId
    ) {
        List<Item> items = excludeWatchListId == null
                ? itemService.findItemsByName(name)
                : itemService.findItemsByNameNotInWatchList(name, excludeWatchListId);
        return items.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

}
