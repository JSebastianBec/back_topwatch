package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.ListTop;
import com.topwatch.back_topwatch.service.ListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
@Tag(name = "Lists", description = "List management endpoints")
public class ListController {

    private final ListService listService;

    @Operation(
            summary = "Create or update a list",
            description = "Creates a new list if no id is provided, updates it otherwise",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/save")
    public ResponseEntity<ListTop> saveOrUpdate(
            @RequestBody ListTop listTop
    ) {
        return ResponseEntity.ok(listService.saveOrUpdateListTop(listTop));
    }

    @Operation(
            summary = "Return all lists",
            description = "Return a list of all register lists",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/lists")
    public List<ListTop> getAllLists() {
        return listService.findAllListTop();
    }

    @Operation(
            summary = "Get given list",
            description = "Get the list given an id",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/list/{id}")
    public ResponseEntity<ListTop> getListTop(
            @PathVariable("id") Long id
    ) {
        Optional<ListTop> list = listService.findListTop(id);
        return list.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Delete list with given id",
            description = "Delete the list with the id provided from the user and his progress",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/delete/{id}")
    public ResponseEntity<String> delete(
            @PathVariable("id") Long id
    ) {
        Optional<ListTop> listTop = listService.findListTop(id);
        if (listTop.isPresent()){
            listService.deleteListTop(listTop.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
