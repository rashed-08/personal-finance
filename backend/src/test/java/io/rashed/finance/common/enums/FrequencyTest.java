package io.rashed.finance.common.enums;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FrequencyTest {

    private final LocalDate date = LocalDate.of(2026, 1, 31);

    @Test
    void daily_advancesByOneDay() {

        assertEquals(LocalDate.of(2026, 2, 1), Frequency.DAILY.advance(date));
    }

    @Test
    void weekly_advancesByOneWeek() {

        assertEquals(LocalDate.of(2026, 2, 7), Frequency.WEEKLY.advance(date));
    }

    @Test
    void monthly_advancesByOneMonth() {

        assertEquals(LocalDate.of(2026, 2, 28), Frequency.MONTHLY.advance(date));
    }

    @Test
    void yearly_advancesByOneYear() {

        assertEquals(LocalDate.of(2027, 1, 31), Frequency.YEARLY.advance(date));
    }
}
