package com.topwatch.back_topwatch.dto;

import jakarta.validation.constraints.NotNull;

public record StartWatchListViewRequest(

        @NotNull(message = "Watch list id is required")
        Long watchListId

) {}
