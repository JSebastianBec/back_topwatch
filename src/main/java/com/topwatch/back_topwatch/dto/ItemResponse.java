package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.Item;

import java.time.Instant;

public record ItemResponse(
        Long id,
        String name,
        Instant creationDate,
        String description,
        String urlAvatar,
        CreatorSummary creator
) {

    public record CreatorSummary(Long id, String nickname) {}

    public static ItemResponse from(Item item) {
        CreatorSummary creator = item.getCreator() != null
                ? new CreatorSummary(item.getCreator().getId(), item.getCreator().getNickname())
                : null;

        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getCreationDate(),
                item.getDescription(),
                item.getUrlAvatar(),
                creator
        );
    }

}
