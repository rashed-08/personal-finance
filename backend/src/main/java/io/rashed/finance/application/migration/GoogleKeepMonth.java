package io.rashed.finance.application.migration;

import io.rashed.finance.common.valueobject.Money;

import java.time.YearMonth;
import java.util.List;

/**
 * One month's worth of parsed Keep lines. {@code statedTotal} is the
 * month's own "=NNNN" (or "Total" / NNNN) line, if present — kept only as a
 * reference figure. Per business decision, it never overrides individual
 * line amounts and a mismatch against the sum of {@code lines} is a
 * non-blocking warning, never a validation failure.
 */
public record GoogleKeepMonth(

        YearMonth yearMonth,

        List<GoogleKeepLine> lines,

        Money statedTotal

) {
}
