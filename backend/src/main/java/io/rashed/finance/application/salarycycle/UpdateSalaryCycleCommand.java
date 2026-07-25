package io.rashed.finance.application.salarycycle;

import io.rashed.finance.domain.salarycycle.SalaryCycleId;

import java.time.LocalDate;

public record UpdateSalaryCycleCommand(

        SalaryCycleId id,

        String name,

        LocalDate salaryDate,

        String description

) {
}
