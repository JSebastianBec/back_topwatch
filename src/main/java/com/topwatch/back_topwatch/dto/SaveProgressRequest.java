package com.topwatch.back_topwatch.dto;

public record SaveProgressRequest(
    Long id,
    Long userId,
    Long listTopId,
    Integer itemsFinished
) {
}
