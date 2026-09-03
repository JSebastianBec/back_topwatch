package com.topwatch.back_topwatch.domain;

import com.topwatch.back_topwatch.domain.enums.Type;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"creator", "categories"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {

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

    @Column(name = "url_avatar")
    private String urlAvatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user_creator", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_categories",
            joinColumns = @JoinColumn(name = "id_item"),
            inverseJoinColumns = @JoinColumn(name = "id_category")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

}
