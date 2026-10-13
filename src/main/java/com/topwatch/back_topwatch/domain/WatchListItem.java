package com.topwatch.back_topwatch.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"creator", "watchList", "item"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "watch_list_items")
public class WatchListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Boolean watched;

    @Column(name = "watched_date")
    private Instant watchedDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_creator", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_watch_list", nullable = false)
    private WatchList watchList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item", nullable = false)
    private Item item;

}
