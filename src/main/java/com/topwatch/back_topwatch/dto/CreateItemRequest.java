package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.enums.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CreateItemRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        String urlAvatar,

        @NotNull(message = "Type is required")
        Type type,

        Set<Long> categoryIds

) {}
