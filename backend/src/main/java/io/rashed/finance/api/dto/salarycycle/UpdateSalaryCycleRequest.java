package io.rashed.finance.api.dto.salarycycle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateSalaryCycleRequest(

        @NotBlank
        String name,

        @NotNull
        LocalDate salaryDate,

        String description
) {
}
