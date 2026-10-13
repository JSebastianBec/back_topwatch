package com.topwatch.back_topwatch.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AddWatchListItemsRequest(

        @NotEmpty(message = "At least one item id is required")
        List<@NotNull(message = "Item id is required") Long> itemIds

) {}
