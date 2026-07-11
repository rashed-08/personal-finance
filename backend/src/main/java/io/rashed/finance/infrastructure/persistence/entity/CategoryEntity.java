package io.rashed.finance.infrastructure.persistence.entity;

import io.rashed.finance.common.enums.CategoryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "categories",
    uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_category_name",
                    columnNames = "name"
            )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType categoryType;

    @Column(nullable = false, name = "is_system")
    private boolean systemDefined;

    @Column(nullable = false, name = "is_active")
    private boolean active;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public CategoryEntity(
            UUID id,
            String name,
            CategoryType categoryType,
            boolean systemDefined,
            boolean active,
            String description,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.categoryType = categoryType;
        this.systemDefined = systemDefined;
        this.active = active;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}