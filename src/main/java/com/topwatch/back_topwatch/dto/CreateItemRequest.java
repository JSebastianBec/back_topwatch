package com.topwatch.back_topwatch.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateItemRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        String urlAvatar

) {}
