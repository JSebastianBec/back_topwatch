package com.topwatch.back_topwatch.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * The watched state of one watch list item inside one user's view. Rows are
 * created the first time the viewer marks the item; no row means not watched.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"watchListView", "watchListItem"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "watch_list_view_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_watch_list_view", "id_watch_list_item"})
)
public class WatchListViewItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Boolean watched;

    @Column(name = "watched_date")
    private Instant watchedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_watch_list_view", nullable = false)
    private WatchListView watchListView;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_watch_list_item", nullable = false)
    private WatchListItem watchListItem;

}
