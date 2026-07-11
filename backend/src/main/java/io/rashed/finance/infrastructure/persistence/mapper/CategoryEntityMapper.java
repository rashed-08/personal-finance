package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.infrastructure.persistence.entity.CategoryEntity;

public final class CategoryEntityMapper {

    private CategoryEntityMapper() {
    }

    public static CategoryEntity toEntity(Category category) {

        if (category == null) {
            return null;
        }

        return new CategoryEntity(
                category.getId().getValue(),
                category.getName(),
                category.getCategoryType(),
                category.isSystemDefined(),
                category.isActive(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public static Category toDomain(CategoryEntity entity) {

        if (entity == null) {
            return null;
        }

        return new Category(
                CategoryId.of(entity.getId()),
                entity.getName(),
                entity.getCategoryType(),
                entity.isSystemDefined(),
                entity.isActive(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}