package com.ledger.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    private String id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "user_id")
    private String userId;

    @PrePersist
    @PreUpdate
    protected void onSave() {
        if (userId == null) userId = "default";
    }
}
