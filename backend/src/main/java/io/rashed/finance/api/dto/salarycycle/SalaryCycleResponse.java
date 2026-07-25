package io.rashed.finance.api.dto.salarycycle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SalaryCycleResponse(

        UUID id,

        String name,

        LocalDate startDate,

        LocalDate endDate,

        LocalDate salaryDate,

        boolean closed,

        String description,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}
