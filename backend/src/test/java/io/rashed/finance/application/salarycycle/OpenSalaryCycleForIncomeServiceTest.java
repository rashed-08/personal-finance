package io.rashed.finance.application.salarycycle;

import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenSalaryCycleForIncomeServiceTest {

    private final LocalDate july10 = LocalDate.of(2026, 7, 10);
    private final LocalDate august10 = LocalDate.of(2026, 8, 10);

    private SalaryCycleRepository repository;
    private OpenSalaryCycleForIncomeService service;

    @BeforeEach
    void setUp() {

        repository = mock(SalaryCycleRepository.class);
        service = new OpenSalaryCycleForIncomeService(repository);

        when(repository.save(any(SalaryCycle.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void execute_opensFirstCycleWhenNoneIsOpen() {

        when(repository.findOpen()).thenReturn(Optional.empty());
        when(repository.existsByName("July 2026")).thenReturn(false);

        SalaryCycleId id = service.execute(july10);

        verify(repository, times(1)).save(any(SalaryCycle.class));
        assertEquals(true, id != null);
    }

    @Test
    void execute_closesPreviouslyOpenCycleTheDayBeforeTheNewSalary() {

        SalaryCycle open = SalaryCycle.open("July 2026", july10, july10, null);
        when(repository.findOpen()).thenReturn(Optional.of(open));
        when(repository.existsByName("August 2026")).thenReturn(false);

        service.execute(august10);

        ArgumentCaptor<SalaryCycle> captor = ArgumentCaptor.forClass(SalaryCycle.class);
        verify(repository, times(2)).save(captor.capture());

        SalaryCycle closedPrevious = captor.getAllValues().get(0);
        assertEquals(august10.minusDays(1), closedPrevious.getEndDate());
        assertEquals(true, closedPrevious.isClosed());

        SalaryCycle newCycle = captor.getAllValues().get(1);
        assertEquals(august10, newCycle.getStartDate());
        assertEquals(true, newCycle.isOpen());
    }

    @Test
    void execute_disambiguatesNameOnCollision() {

        when(repository.findOpen()).thenReturn(Optional.empty());
        when(repository.existsByName("July 2026")).thenReturn(true);

        service.execute(july10);

        ArgumentCaptor<SalaryCycle> captor = ArgumentCaptor.forClass(SalaryCycle.class);
        verify(repository).save(captor.capture());

        assertEquals("July 2026 (" + july10 + ")", captor.getValue().getName());
    }

    @Test
    void execute_rejectsClosingWhenNewSalaryPredatesTheOpenCycle() {

        SalaryCycle open = SalaryCycle.open("August 2026", august10, august10, null);
        when(repository.findOpen()).thenReturn(Optional.of(open));

        assertThrows(IllegalArgumentException.class, () -> service.execute(july10));
    }
}
