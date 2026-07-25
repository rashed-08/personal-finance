package io.rashed.finance.application.salarycycle;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CloseSalaryCycleServiceTest {

    private final LocalDate july10 = LocalDate.of(2026, 7, 10);

    private SalaryCycleRepository repository;
    private CloseSalaryCycleService service;

    @BeforeEach
    void setUp() {

        repository = mock(SalaryCycleRepository.class);
        service = new CloseSalaryCycleService(repository);

        when(repository.save(any(SalaryCycle.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void execute_closesAnOpenCycle() {

        SalaryCycle cycle = SalaryCycle.open("July 2026", july10, july10, null);
        when(repository.findById(cycle.getId())).thenReturn(Optional.of(cycle));

        SalaryCycle closed = service.execute(cycle.getId(), july10.plusDays(30));

        assertEquals(july10.plusDays(30), closed.getEndDate());
        assertEquals(true, closed.isClosed());
    }

    @Test
    void execute_rejectsUnknownCycle() {

        SalaryCycleId id = SalaryCycleId.newId();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id, july10));
    }
}
