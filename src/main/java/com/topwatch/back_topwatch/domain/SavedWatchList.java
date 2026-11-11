package com.topwatch.back_topwatch.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A watch list created by someone else that a user added to their own
 * "my watch lists". The list itself stays owned (and editable) only by its creator.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "watchList"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "saved_watch_lists",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_user", "id_watch_list"})
)
public class SavedWatchList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @CreationTimestamp
    @Column(name = "saved_date", nullable = false, updatable = false)
    private Instant savedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_watch_list", nullable = false)
    private WatchList watchList;

}
