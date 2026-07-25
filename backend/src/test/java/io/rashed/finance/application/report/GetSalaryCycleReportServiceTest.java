package io.rashed.finance.application.report;

import io.rashed.finance.application.salarycycle.CalculateCarryForwardService;
import io.rashed.finance.application.salarycycle.CarryForwardResult;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetSalaryCycleReportServiceTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private SalaryCycleRepository salaryCycleRepository;
    private CalculateCarryForwardService calculateCarryForwardService;
    private GetSalaryCycleReportService service;

    @BeforeEach
    void setUp() {

        salaryCycleRepository = mock(SalaryCycleRepository.class);
        calculateCarryForwardService = mock(CalculateCarryForwardService.class);
        service = new GetSalaryCycleReportService(salaryCycleRepository, calculateCarryForwardService);
    }

    @Test
    void execute_rejectsUnknownCycle() {

        SalaryCycleId id = SalaryCycleId.newId();
        when(salaryCycleRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id));
    }

    @Test
    void execute_combinesCycleMetadataWithCarryForward() {

        SalaryCycle cycle = SalaryCycle.create("July 2026", today, today.plusDays(30), today, null);
        when(salaryCycleRepository.findById(cycle.getId())).thenReturn(Optional.of(cycle));

        CarryForwardResult carryForward = new CarryForwardResult(
                cycle.getId(), Money.of(1000), Money.of(5000), Money.of(3000), Money.zero(), Money.of(3000));
        when(calculateCarryForwardService.execute(cycle.getId())).thenReturn(carryForward);

        SalaryCycleReportResult result = service.execute(cycle.getId());

        assertEquals(cycle.getName(), result.cycleName());
        assertEquals(Money.of(1000), result.openingBalance());
        assertEquals(Money.of(5000), result.income());
        assertEquals(Money.of(3000), result.expenses());
        assertEquals(Money.of(3000), result.closingBalance());
    }
}
