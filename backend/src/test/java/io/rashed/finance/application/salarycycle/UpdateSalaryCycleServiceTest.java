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

class UpdateSalaryCycleServiceTest {

    private final LocalDate july10 = LocalDate.of(2026, 7, 10);

    private SalaryCycleRepository repository;
    private UpdateSalaryCycleService service;

    @BeforeEach
    void setUp() {

        repository = mock(SalaryCycleRepository.class);
        service = new UpdateSalaryCycleService(repository);

        when(repository.save(any(SalaryCycle.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void execute_updatesNameSalaryDateAndDescription() {

        SalaryCycle cycle = SalaryCycle.open("July 2026", july10, july10, null);
        when(repository.findById(cycle.getId())).thenReturn(Optional.of(cycle));

        SalaryCycle updated = service.execute(
                new UpdateSalaryCycleCommand(cycle.getId(), "Renamed", july10.plusDays(1), "note"));

        assertEquals("Renamed", updated.getName());
        assertEquals(july10.plusDays(1), updated.getSalaryDate());
        assertEquals("note", updated.getDescription());
    }

    @Test
    void execute_rejectsUnknownCycle() {

        SalaryCycleId id = SalaryCycleId.newId();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.execute(new UpdateSalaryCycleCommand(id, "x", july10, null)));
    }
}
