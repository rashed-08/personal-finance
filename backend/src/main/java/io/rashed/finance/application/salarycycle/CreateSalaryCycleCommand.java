package io.rashed.finance.application.salarycycle;

import java.time.LocalDate;

public record CreateSalaryCycleCommand(

        String name,

        LocalDate startDate,

        LocalDate endDate,

        LocalDate salaryDate,

        String description

) {
}
