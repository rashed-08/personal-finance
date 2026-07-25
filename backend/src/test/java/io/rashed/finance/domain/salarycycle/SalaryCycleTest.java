package io.rashed.finance.domain.salarycycle;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SalaryCycleTest {

    private final LocalDate july10 = LocalDate.of(2026, 7, 10);
    private final LocalDate august9 = LocalDate.of(2026, 8, 9);
    private final LocalDate august10 = LocalDate.of(2026, 8, 10);

    @Test
    void open_createsAnOngoingCycleWithNoEndDate() {

        SalaryCycle cycle = SalaryCycle.open("July 2026", july10, july10, null);

        assertNull(cycle.getEndDate());
        assertTrue(cycle.isOpen());
        assertFalse(cycle.isClosed());
    }

    @Test
    void create_withEndDateProducesAClosedCycle() {

        SalaryCycle cycle = SalaryCycle.create("July 2026", july10, august9, july10, null);

        assertEquals(august9, cycle.getEndDate());
        assertTrue(cycle.isClosed());
    }

    @Test
    void create_withoutEndDateProducesAnOpenCycle() {

        SalaryCycle cycle = SalaryCycle.create("July 2026", july10, null, july10, null);

        assertTrue(cycle.isOpen());
    }

    @Test
    void constructor_rejectsEndDateBeforeStartDate() {

        assertThrows(IllegalArgumentException.class, () ->
                new SalaryCycle(SalaryCycleId.newId(), "x", july10, july10.minusDays(1), july10, true, null,
                        java.time.LocalDateTime.now(), java.time.LocalDateTime.now()));
    }

    @Test
    void constructor_rejectsClosedWithoutEndDate() {

        assertThrows(IllegalArgumentException.class, () ->
                new SalaryCycle(SalaryCycleId.newId(), "x", july10, null, july10, true, null,
                        java.time.LocalDateTime.now(), java.time.LocalDateTime.now()));
    }

    @Test
    void containsDate_openCycleContainsAnyDateOnOrAfterStart() {

        SalaryCycle cycle = SalaryCycle.open("July 2026", july10, july10, null);

        assertFalse(cycle.containsDate(july10.minusDays(1)));
        assertTrue(cycle.containsDate(july10));
        assertTrue(cycle.containsDate(LocalDate.of(2030, 1, 1)));
    }

    @Test
    void containsDate_closedCycleIsBoundedByEndDate() {

        SalaryCycle cycle = SalaryCycle.create("July 2026", july10, august9, july10, null);

        assertTrue(cycle.containsDate(august9));
        assertFalse(cycle.containsDate(august10));
    }

    @Test
    void close_setsEndDateAndMarksClosed() {

        SalaryCycle cycle = SalaryCycle.open("July 2026", july10, july10, null);

        SalaryCycle closed = cycle.close(august9);

        assertTrue(closed.isClosed());
        assertEquals(august9, closed.getEndDate());
    }

    @Test
    void close_rejectsAlreadyClosedCycle() {

        SalaryCycle closed = SalaryCycle.open("July 2026", july10, july10, null).close(august9);

        assertThrows(IllegalStateException.class, () -> closed.close(august10));
    }

    @Test
    void close_rejectsEndDateBeforeStart() {

        SalaryCycle cycle = SalaryCycle.open("July 2026", july10, july10, null);

        assertThrows(IllegalArgumentException.class, () -> cycle.close(july10.minusDays(1)));
    }

    @Test
    void reopen_clearsEndDateAndMarksOpen() {

        SalaryCycle closed = SalaryCycle.open("July 2026", july10, july10, null).close(august9);

        SalaryCycle reopened = closed.reopen();

        assertTrue(reopened.isOpen());
        assertNull(reopened.getEndDate());
    }

    @Test
    void reopen_rejectsAlreadyOpenCycle() {

        SalaryCycle open = SalaryCycle.open("July 2026", july10, july10, null);

        assertThrows(IllegalStateException.class, open::reopen);
    }

    @Test
    void update_changesNameSalaryDateAndDescriptionOnly() {

        SalaryCycle cycle = SalaryCycle.open("July 2026", july10, july10, null);

        SalaryCycle updated = cycle.update("Renamed", august9, "notes");

        assertEquals("Renamed", updated.getName());
        assertEquals(august9, updated.getSalaryDate());
        assertEquals("notes", updated.getDescription());
        assertEquals(july10, updated.getStartDate());
    }

    @Test
    void isCurrent_reflectsWhetherTodayFallsInsideTheCycle() {

        SalaryCycle pastClosedCycle = SalaryCycle.create(
                "Old", LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 31), LocalDate.of(2000, 1, 1), null);

        assertFalse(pastClosedCycle.isCurrent());
    }

    @Test
    void validation_rejectsBlankName() {

        assertThrows(IllegalArgumentException.class, () -> SalaryCycle.open("  ", july10, july10, null));
    }

    @Test
    void validation_rejectsInvalidSalaryDate() {

        assertThrows(IllegalArgumentException.class, () ->
                SalaryCycle.open("x", july10, LocalDate.of(1990, 1, 1), null));
    }
}
