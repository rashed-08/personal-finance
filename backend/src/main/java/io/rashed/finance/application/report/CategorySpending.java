package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.categories.CategoryId;

public record CategorySpending(

        CategoryId categoryId,

        String categoryName,

        Money totalSpent

) {
}
