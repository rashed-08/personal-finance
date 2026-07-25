package io.rashed.finance.api.dto.salarycycle;

import io.rashed.finance.application.salarycycle.CreateSalaryCycleCommand;
import io.rashed.finance.common.valueobject.DateRange;
import io.rashed.finance.domain.salarycycle.SalaryCycle;

import java.util.List;

public final class SalaryCycleDtoMapper {

    private SalaryCycleDtoMapper() {
    }

    public static CreateSalaryCycleCommand toCommand(CreateSalaryCycleRequest request) {

        return new CreateSalaryCycleCommand(
                request.name(),
                DateRange.of(request.startDate(), request.endDate()),
                request.salaryDate(),
                request.description()
        );
    }

    public static SalaryCycleResponse toResponse(SalaryCycle salaryCycle) {

        return new SalaryCycleResponse(
                salaryCycle.getId().getValue(),
                salaryCycle.getName(),
                salaryCycle.getPeriod().getStartDate(),
                salaryCycle.getPeriod().getEndDate(),
                salaryCycle.getSalaryDate(),
                salaryCycle.isClosed(),
                salaryCycle.getDescription(),
                salaryCycle.getCreatedAt(),
                salaryCycle.getUpdatedAt()
        );
    }

    public static List<SalaryCycleResponse> toResponseList(List<SalaryCycle> salaryCycles) {

        return salaryCycles.stream()
                .map(SalaryCycleDtoMapper::toResponse)
                .toList();
    }
}
