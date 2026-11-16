package com.topwatch.back_topwatch.controller;

import com.topwatch.back_topwatch.domain.User;
import com.topwatch.back_topwatch.domain.enums.ImageFolder;
import com.topwatch.back_topwatch.dto.DeleteImageRequest;
import com.topwatch.back_topwatch.service.ImageStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image upload endpoints")
public class ImageController {

    private final ImageStorageService storage;

    @Operation(summary = "Upload image", security = @SecurityRequirement(name = "Bearer Token"))
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") ImageFolder folder,
            @AuthenticationPrincipal User currentUser
    ) {
        String url = storage.upload(file, folder, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
    }

    @Operation(summary = "Delete own image", security = @SecurityRequirement(name = "Bearer Token"))
    @PostMapping("/delete")
    public ResponseEntity<Void> delete(
            @Valid @RequestBody DeleteImageRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        storage.deleteIfOwned(request.url(), currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}