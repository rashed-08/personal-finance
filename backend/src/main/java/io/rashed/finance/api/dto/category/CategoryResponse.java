package io.rashed.finance.api.dto.category;

import io.rashed.finance.common.enums.CategoryType;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(

        UUID id,

        String name,

        CategoryType categoryType,

        boolean systemDefined,

        boolean active,

        String description,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}