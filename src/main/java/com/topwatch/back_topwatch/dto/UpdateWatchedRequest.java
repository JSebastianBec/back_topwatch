package com.topwatch.back_topwatch.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateWatchedRequest(

        @NotNull(message = "Watched is required")
        Boolean watched

) {}
