package io.rashed.finance.api.dto.salarycycle;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CloseSalaryCycleRequest(

        @NotNull
        LocalDate endDate
) {
}
