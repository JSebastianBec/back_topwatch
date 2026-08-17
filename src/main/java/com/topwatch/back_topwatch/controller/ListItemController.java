package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.ListItem;
import com.topwatch.back_topwatch.service.ListItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/list-items")
@RequiredArgsConstructor
@Tag(name = "List Items", description = "List-Item relationship management endpoints")
public class ListItemController {

    private final ListItemService listItemService;

    @Operation(
            summary = "Add item to list",
            description = "Creates a new list-item entry",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/save")
    public ResponseEntity<ListItem> save(
            @RequestBody ListItem listItem
    ) {
        return ResponseEntity.ok(listItemService.save(listItem));
    }

    @Operation(
            summary = "Get list-item by id",
            description = "Get the list-item entry given an id",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/{id}")
    public ResponseEntity<ListItem> getById(
            @PathVariable("id") Long id
    ) {
        Optional<ListItem> listItem = listItemService.findById(id);
        return listItem.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get items by list",
            description = "Return all items belonging to a given list",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/list/{listId}")
    public List<ListItem> getByList(
            @PathVariable("listId") Long listId
    ) {
        return listItemService.findByListId(listId);
    }

    @Operation(
            summary = "Get lists by item",
            description = "Return all list entries that contain a given item",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/item/{itemId}")
    public List<ListItem> getByItem(
            @PathVariable("itemId") Long itemId
    ) {
        return listItemService.findByItemId(itemId);
    }

    @Operation(
            summary = "Mark item as watched",
            description = "Sets watched to true and records the watched date",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/watch/{id}")
    public ResponseEntity<ListItem> markAsWatched(
            @PathVariable("id") Long id
    ) {
        Optional<ListItem> listItem = listItemService.findById(id);
        if (listItem.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(listItemService.markAsWatched(id));
    }

    @Operation(
            summary = "Remove item from list",
            description = "Delete the list-item entry with the id provided",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id
    ) {
        Optional<ListItem> listItem = listItemService.findById(id);
        if (listItem.isPresent()) {
            listItemService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
