package io.rashed.finance.common.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyTest {

    @Test
    void of_roundsToTwoDecimalPlaces() {

        Money money = Money.of(new BigDecimal("10.005"));

        assertEquals(new BigDecimal("10.01"), money.getAmount());
    }

    @Test
    void add_sumsTwoAmounts() {

        assertEquals(Money.of(300), Money.of(100).add(Money.of(200)));
    }

    @Test
    void subtract_computesDifference() {

        assertEquals(Money.of(-50), Money.of(100).subtract(Money.of(150)));
    }

    @Test
    void equals_ignoresScaleDifferences() {

        assertEquals(Money.of("500"), Money.of("500.00"));
    }

    @Test
    void equals_isValueBased() {

        assertTrue(Money.of(500).equals(Money.of(500)));
        assertFalse(Money.of(500).equals(Money.of(501)));
    }

    @Test
    void hashCode_isConsistentWithEquals() {

        assertEquals(Money.of("500").hashCode(), Money.of("500.00").hashCode());
    }

    @Test
    void isPositive_isNegative_isZero() {

        assertTrue(Money.of(1).isPositive());
        assertTrue(Money.of(-1).isNegative());
        assertTrue(Money.zero().isZero());
    }

    @Test
    void comparisons_reflectOrdering() {

        assertTrue(Money.of(200).greaterThan(Money.of(100)));
        assertTrue(Money.of(100).lessThan(Money.of(200)));
        assertTrue(Money.of(100).greaterThanOrEqual(Money.of(100)));
        assertTrue(Money.of(100).lessThanOrEqual(Money.of(100)));
    }

    @Test
    void of_rejectsNullAmount() {

        assertThrows(NullPointerException.class, () -> Money.of((BigDecimal) null));
    }

    @Test
    void add_rejectsNullOperand() {

        assertThrows(NullPointerException.class, () -> Money.of(100).add(null));
    }

    @Test
    void divide_roundsHalfUp() {

        assertEquals(Money.of("3.33"), Money.of(10).divide(new BigDecimal(3)));
    }

    @Test
    void abs_returnsPositiveValue() {

        assertEquals(Money.of(50), Money.of(-50).abs());
    }

    @Test
    void negate_flipsSign() {

        assertEquals(Money.of(-50), Money.of(50).negate());
    }
}
