package io.rashed.finance.api.dto.category;

import io.rashed.finance.common.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        CategoryType categoryType,

        boolean systemDefined,

        @Size(max = 500)
        String description

) {
}