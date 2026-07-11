package io.rashed.finance.api.dto.category;

import io.rashed.finance.application.category.CreateCategoryCommand;
import io.rashed.finance.domain.categories.Category;

public final class CategoryDtoMapper {

    private CategoryDtoMapper() {
    }

    public static CreateCategoryCommand toCommand(
            String name,
            io.rashed.finance.common.enums.CategoryType categoryType,
            boolean systemDefined,
            String description
    ) {

        return new CreateCategoryCommand(
                name,
                categoryType,
                systemDefined,
                description
        );
    }

    public static CategoryResponse toResponse(
            Category category
    ) {

        return new CategoryResponse(

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
}