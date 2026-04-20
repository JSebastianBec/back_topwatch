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
@Table(name = "lists")
public class ListTop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_creator_id")
    private User userCreator;
    private String name;
    private Date creationDate;
    private String description;
    @Enumerated(EnumType.STRING)
    private Type type;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "list_items",
        joinColumns = @JoinColumn(name = "list_id"),
        inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private java.util.List<Item> items;
}
