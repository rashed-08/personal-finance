package io.rashed.finance.application.salarycycle;

import io.rashed.finance.common.valueobject.DateRange;

import java.time.LocalDate;

public record CreateSalaryCycleCommand(

        String name,

        DateRange period,

        LocalDate salaryDate,

        String description

) {
}