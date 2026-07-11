package io.rashed.finance.application.category;

import io.rashed.finance.common.enums.CategoryType;

public record CreateCategoryCommand(String name, CategoryType categoryType, boolean systemDefined, String description) {

}