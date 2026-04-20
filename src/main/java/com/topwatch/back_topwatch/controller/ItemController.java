package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Item management endpoints")
public class ItemController {

    private final ItemService itemService;

    @Operation(
            summary = "Create or update an item",
            description = "Creates a new item if no id is provided, updates it otherwise",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/save")
    public ResponseEntity<Item> saveOrUpdate(
            @RequestBody Item item
    ) {
        return ResponseEntity.ok(itemService.saveOrUpdateItem(item));
    }

    @Operation(
            summary = "Return all items",
            description = "Return a list of all registered items",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/items")
    public List<Item> getAllItems() {
        return itemService.getAllItem();
    }

    @Operation(
            summary = "Get item by id",
            description = "Get the item given an id",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/item/{id}")
    public ResponseEntity<Item> getItem(
            @PathVariable("id") Long id
    ) {
        Optional<Item> item = itemService.getItem(id);
        return item.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Delete item with given id",
            description = "Delete the item with the id provided",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id
    ) {
        Optional<Item> item = itemService.getItem(id);
        if (item.isPresent()) {
            itemService.deleteItem(item.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
