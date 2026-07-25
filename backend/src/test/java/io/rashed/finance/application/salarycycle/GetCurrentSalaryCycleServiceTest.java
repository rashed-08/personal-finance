package io.rashed.finance.application.salarycycle;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetCurrentSalaryCycleServiceTest {

    private SalaryCycleRepository repository;
    private GetCurrentSalaryCycleService service;

    @BeforeEach
    void setUp() {

        repository = mock(SalaryCycleRepository.class);
        service = new GetCurrentSalaryCycleService(repository);
    }

    @Test
    void execute_returnsTheCurrentCycle() {

        LocalDate today = LocalDate.of(2026, 7, 25);
        SalaryCycle cycle = SalaryCycle.open("July 2026", today, today, null);
        when(repository.findCurrent()).thenReturn(Optional.of(cycle));

        assertEquals(cycle, service.execute());
    }

    @Test
    void execute_throwsWhenNoCycleIsCurrent() {

        when(repository.findCurrent()).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute());
    }
}
