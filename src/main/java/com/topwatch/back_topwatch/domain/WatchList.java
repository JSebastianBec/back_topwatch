package com.topwatch.back_topwatch.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "creator")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "watch_lists")
public class WatchList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Instant creationDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_creator", nullable = false)
    private User creator;

}
