package io.rashed.finance.application.salarycycle;

import io.rashed.finance.domain.salarycycle.SalaryCycle;
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

class CreateSalaryCycleServiceTest {

    private final LocalDate july10 = LocalDate.of(2026, 7, 10);

    private SalaryCycleRepository repository;
    private CreateSalaryCycleService service;

    @BeforeEach
    void setUp() {

        repository = mock(SalaryCycleRepository.class);
        service = new CreateSalaryCycleService(repository);

        when(repository.save(any(SalaryCycle.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void create_rejectsDuplicateName() {

        when(repository.existsByName("July 2026")).thenReturn(true);

        CreateSalaryCycleCommand command = new CreateSalaryCycleCommand(
                "July 2026", july10, july10.plusDays(30), july10, null);

        assertThrows(IllegalArgumentException.class, () -> service.create(command));
    }

    @Test
    void create_rejectsOpenCycleWhenAnotherIsAlreadyOpen() {

        when(repository.existsByName("July 2026")).thenReturn(false);
        when(repository.findOpen()).thenReturn(Optional.of(
                SalaryCycle.open("June 2026", july10.minusMonths(1), july10.minusMonths(1), null)));

        CreateSalaryCycleCommand command = new CreateSalaryCycleCommand(
                "July 2026", july10, null, july10, null);

        assertThrows(IllegalStateException.class, () -> service.create(command));
    }

    @Test
    void create_allowsClosedCycleEvenWhenAnotherIsOpen() {

        when(repository.existsByName("Backfill")).thenReturn(false);

        CreateSalaryCycleCommand command = new CreateSalaryCycleCommand(
                "Backfill", july10.minusYears(1), july10.minusYears(1).plusDays(30), july10.minusYears(1), null);

        SalaryCycle created = service.create(command);

        assertEquals("Backfill", created.getName());
    }
}
