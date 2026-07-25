package io.rashed.finance.domain.funds;

import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.common.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FundTest {

    private final LocalDate targetDate = LocalDate.of(2027, 1, 1);

    @Test
    void create_startsActiveWithoutTarget() {

        Fund fund = Fund.create("Vacation Fund", FundType.SAVINGS, null, null, null);

        assertTrue(fund.isActive());
        assertFalse(fund.hasTargetAmount());
        assertNull(fund.getTargetDate());
        assertEquals(FundType.SAVINGS, fund.getFundType());
    }

    @Test
    void create_rejectsBlankName() {

        assertThrows(IllegalArgumentException.class, () ->
                Fund.create("  ", FundType.CUSTOM, null, null, null));
    }

    @Test
    void create_rejectsZeroTargetAmount() {

        assertThrows(IllegalArgumentException.class, () ->
                Fund.create("Fund", FundType.SAVINGS, Money.zero(), null, null));
    }

    @Test
    void create_rejectsNegativeTargetAmount() {

        assertThrows(IllegalArgumentException.class, () ->
                Fund.create("Fund", FundType.SAVINGS, Money.of(-100), null, null));
    }

    @Test
    void create_acceptsPositiveTargetAmountAndDate() {

        Fund fund = Fund.create("New Laptop", FundType.GOAL, Money.of(1500), targetDate, null);

        assertTrue(fund.hasTargetAmount());
        assertEquals(Money.of(1500), fund.getTargetAmount());
        assertEquals(targetDate, fund.getTargetDate());
    }

    @Test
    void emergencyFund_isPresetEmergencyType() {

        assertTrue(Fund.emergencyFund().isEmergencyFund());
    }

    @Test
    void zakatFund_isPresetZakatType() {

        assertTrue(Fund.zakatFund().isZakatFund());
    }

    @Test
    void savingsFund_isPresetSavingsTypeWithTarget() {

        Fund fund = Fund.savingsFund("House Down Payment", Money.of(50000), targetDate);

        assertTrue(fund.isSavingsFund());
        assertEquals(Money.of(50000), fund.getTargetAmount());
    }

    @Test
    void rename_changesNamePreservingIdentity() {

        Fund fund = Fund.create("Old Name", FundType.CUSTOM, null, null, null);

        Fund renamed = fund.rename("New Name");

        assertEquals("New Name", renamed.getName());
        assertEquals(fund.getId(), renamed.getId());
    }

    @Test
    void changeTarget_replacesAmountAndDate() {

        Fund fund = Fund.create("Fund", FundType.GOAL, Money.of(1000), targetDate, null);

        Fund changed = fund.changeTarget(Money.of(2000), targetDate.plusMonths(6));

        assertEquals(Money.of(2000), changed.getTargetAmount());
        assertEquals(targetDate.plusMonths(6), changed.getTargetDate());
    }

    @Test
    void changeTarget_rejectsZeroAmount() {

        Fund fund = Fund.create("Fund", FundType.GOAL, Money.of(1000), targetDate, null);

        assertThrows(IllegalArgumentException.class, () -> fund.changeTarget(Money.zero(), targetDate));
    }

    @Test
    void deactivate_thenActivate_roundTrips() {

        Fund fund = Fund.create("Fund", FundType.CUSTOM, null, null, null);

        Fund deactivated = fund.deactivate();
        assertFalse(deactivated.isActive());
        assertTrue(deactivated.isInactive());

        Fund reactivated = deactivated.activate();
        assertTrue(reactivated.isActive());
    }

    @Test
    void deactivate_isIdempotent() {

        Fund deactivated = Fund.create("Fund", FundType.CUSTOM, null, null, null).deactivate();

        assertSame(deactivated, deactivated.deactivate());
    }

    @Test
    void activate_isIdempotent() {

        Fund fund = Fund.create("Fund", FundType.CUSTOM, null, null, null);

        assertSame(fund, fund.activate());
    }
}
