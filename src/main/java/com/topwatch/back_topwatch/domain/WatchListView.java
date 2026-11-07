package com.topwatch.back_topwatch.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A user's own run through a watch list (theirs or someone else's). The list
 * itself is never touched; what the viewer has watched lives in WatchListViewItem.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"viewer", "watchList"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "watch_list_views",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user_viewer", "id_watch_list"})
)
public class WatchListView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @CreationTimestamp
    @Column(name = "start_date", nullable = false, updatable = false)
    private Instant startDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_viewer", nullable = false)
    private User viewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_watch_list", nullable = false)
    private WatchList watchList;

}
