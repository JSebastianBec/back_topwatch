package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.Progress;
import com.topwatch.back_topwatch.dto.SaveProgressRequest;
import com.topwatch.back_topwatch.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Tag(name = "Progress", description = "Progress management endpoints")
public class ProgressController {

    private final ProgressService progressService;

    @Operation(
            summary = "Create or update progress",
            description = "Creates a new progress entry if no id is provided, updates it otherwise",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/save")
    public ResponseEntity<Progress> saveOrUpdate(
            @RequestBody SaveProgressRequest request
    ) {
        return ResponseEntity.ok(progressService.saveFromRequest(request));
    }

    @Operation(
            summary = "Get progress by id",
            description = "Get the progress entry given an id",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/progress/{id}")
    public ResponseEntity<Progress> getProgress(
            @PathVariable("id") Long id
    ) {
        Optional<Progress> progress = progressService.findById(id);
        return progress.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get progress by user",
            description = "Return all progress entries for a given user id",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/user/{userId}")
    public List<Progress> getByUser(
            @PathVariable("userId") Long userId
    ) {
        return progressService.findByUserId(userId);
    }

    @Operation(
            summary = "Get progress by list",
            description = "Return all progress entries for a given list id",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @GetMapping("/list/{listId}")
    public List<Progress> getByList(
            @PathVariable("listId") Long listId
    ) {
        return progressService.findByListId(listId);
    }

    @Operation(
            summary = "Delete progress with given id",
            description = "Delete the progress entry with the id provided",
            security = @SecurityRequirement(name = "Bearer Token")
    )
    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id
    ) {
        Optional<Progress> progress = progressService.findById(id);
        if (progress.isPresent()) {
            progressService.delete(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
