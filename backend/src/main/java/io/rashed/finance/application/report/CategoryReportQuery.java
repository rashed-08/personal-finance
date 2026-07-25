package io.rashed.finance.application.report;

import io.rashed.finance.domain.categories.CategoryId;

import java.time.LocalDate;

public record CategoryReportQuery(

        CategoryId categoryId,

        LocalDate fromDate,

        LocalDate toDate

) {
}
