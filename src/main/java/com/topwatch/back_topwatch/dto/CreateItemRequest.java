package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.enums.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateItemRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        @Size(max = 2048, message = "Image URL is too long")
        @Pattern(regexp = "^https://.+", message = "Image URL must start with https://")
        String urlAvatar,

        @NotNull(message = "Type is required")
        Type type,

        Set<Long> categoryIds

) {}
