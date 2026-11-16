package com.topwatch.back_topwatch.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddToMyWatchListsRequest(

        @NotEmpty(message = "At least one watch list id is required")
        List<@NotNull(message = "Watch list id is required") Long> watchListIds

) {}
