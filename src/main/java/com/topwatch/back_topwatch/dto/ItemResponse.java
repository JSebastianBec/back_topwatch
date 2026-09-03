package com.topwatch.back_topwatch.dto;

import com.topwatch.back_topwatch.domain.Item;
import com.topwatch.back_topwatch.domain.enums.Type;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record ItemResponse(
        Long id,
        String name,
        Instant creationDate,
        String description,
        String urlAvatar,
        Type type,
        CreatorSummary creator,
        Set<CategorySummary> categories
) {

    public record CreatorSummary(Long id, String nickname) {}

    public record CategorySummary(Long id, String name, String description) {}

    public static ItemResponse from(Item item) {
        CreatorSummary creator = item.getCreator() != null
                ? new CreatorSummary(item.getCreator().getId(), item.getCreator().getNickname())
                : null;

        Set<CategorySummary> categories = item.getCategories().stream()
                .map(category -> new CategorySummary(category.getId(), category.getName(), category.getDescription()))
                .collect(Collectors.toSet());

        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getCreationDate(),
                item.getDescription(),
                item.getUrlAvatar(),
                item.getType(),
                creator,
                categories
        );
    }

}
