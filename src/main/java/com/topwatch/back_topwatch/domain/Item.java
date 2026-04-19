package com.topwatch.back_topwatch.domain;

import com.topwatch.back_topwatch.domain.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_creator_id")
    private User userCreator;
    private String name;
    private Date creationDate;
    private String description;
    private String avatarURL;
    @Enumerated(EnumType.STRING)
    private Type type;
    @ManyToMany(mappedBy = "items", fetch = FetchType.LAZY)
    private java.util.List<ListTop> listTops;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "item_categories",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private java.util.List<Category> categories;
}
