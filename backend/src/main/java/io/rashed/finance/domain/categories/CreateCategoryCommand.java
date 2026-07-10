package io.rashed.finance.domain.categories;

import io.rashed.finance.common.enums.CategoryType;

public record CreateCategoryCommand(String name, CategoryType categoryType, boolean systemDefined, String description) {

}