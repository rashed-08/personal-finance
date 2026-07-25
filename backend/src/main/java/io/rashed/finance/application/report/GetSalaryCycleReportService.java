package io.rashed.finance.application.report;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.application.salarycycle.CalculateCarryForwardService;
import io.rashed.finance.application.salarycycle.CarryForwardResult;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;

/** Thin wrapper over the existing CalculateCarryForwardService plus cycle metadata. */
@Service
public class GetSalaryCycleReportService {

    private final SalaryCycleRepository salaryCycleRepository;
    private final CalculateCarryForwardService calculateCarryForwardService;

    public GetSalaryCycleReportService(
            SalaryCycleRepository salaryCycleRepository,
            CalculateCarryForwardService calculateCarryForwardService
    ) {
        this.salaryCycleRepository = Objects.requireNonNull(salaryCycleRepository);
        this.calculateCarryForwardService = Objects.requireNonNull(calculateCarryForwardService);
    }

    public SalaryCycleReportResult execute(SalaryCycleId id) {

        SalaryCycle cycle = salaryCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary cycle not found."));

        CarryForwardResult result = calculateCarryForwardService.execute(id);

        return new SalaryCycleReportResult(
                id,
                cycle.getName(),
                cycle.getStartDate(),
                cycle.getEndDate(),
                cycle.isClosed(),
                result.openingBalance(),
                result.income(),
                result.expenses(),
                result.adjustments(),
                result.closingBalance()
        );
    }
}
