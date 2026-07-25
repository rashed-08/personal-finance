package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.common.valueobject.DateRange;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.infrastructure.persistence.entity.SalaryCycleEntity;

import java.math.BigDecimal;

public final class SalaryCycleEntityMapper {

    private SalaryCycleEntityMapper() {
    }

    public static SalaryCycleEntity toEntity(SalaryCycle salaryCycle) {

        if (salaryCycle == null) {
            return null;
        }

        return new SalaryCycleEntity(
                salaryCycle.getId().getValue(),
                salaryCycle.getName(),
                salaryCycle.getPeriod().getStartDate(),
                salaryCycle.getPeriod().getEndDate(),
                salaryCycle.getSalaryDate(),
                BigDecimal.ZERO,
                salaryCycle.isClosed(),
                salaryCycle.getDescription(),
                salaryCycle.getCreatedAt(),
                salaryCycle.getUpdatedAt()
        );
    }

    public static SalaryCycle toDomain(SalaryCycleEntity entity) {

        if (entity == null) {
            return null;
        }

        return new SalaryCycle(
                SalaryCycleId.of(entity.getId()),
                entity.getCycleName(),
                DateRange.of(entity.getCycleStartDate(), entity.getCycleEndDate()),
                entity.getSalaryReceivedDate(),
                entity.isClosed(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
