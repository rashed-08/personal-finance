package io.rashed.finance.application.salarycycle;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReopenSalaryCycleServiceTest {

    private final LocalDate july10 = LocalDate.of(2026, 7, 10);

    private SalaryCycleRepository repository;
    private ReopenSalaryCycleService service;

    @BeforeEach
    void setUp() {

        repository = mock(SalaryCycleRepository.class);
        service = new ReopenSalaryCycleService(repository);

        when(repository.save(any(SalaryCycle.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void execute_reopensAClosedCycleWhenNoneIsCurrentlyOpen() {

        SalaryCycle closed = SalaryCycle.open("July 2026", july10, july10, null).close(july10.plusDays(30));
        when(repository.findById(closed.getId())).thenReturn(Optional.of(closed));
        when(repository.findOpen()).thenReturn(Optional.empty());

        SalaryCycle reopened = service.execute(closed.getId());

        assertTrue(reopened.isOpen());
    }

    @Test
    void execute_rejectsWhenAnotherCycleIsAlreadyOpen() {

        SalaryCycle closed = SalaryCycle.open("July 2026", july10, july10, null).close(july10.plusDays(30));
        SalaryCycle otherOpen = SalaryCycle.open("August 2026", july10.plusDays(31), july10.plusDays(31), null);

        when(repository.findById(closed.getId())).thenReturn(Optional.of(closed));
        when(repository.findOpen()).thenReturn(Optional.of(otherOpen));

        assertThrows(IllegalStateException.class, () -> service.execute(closed.getId()));
    }

    @Test
    void execute_rejectsUnknownCycle() {

        SalaryCycleId id = SalaryCycleId.newId();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id));
    }
}
