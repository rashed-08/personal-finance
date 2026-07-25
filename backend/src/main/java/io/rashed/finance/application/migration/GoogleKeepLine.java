package io.rashed.finance.application.migration;

import io.rashed.finance.common.valueobject.Money;

/**
 * One parsed expense line from a Google Keep note, e.g. "Market 10735
 * (690+1350+...)". {@code breakdownNotes} preserves the original itemized
 * text (whether it came from parentheses or a bare "+"-joined expression)
 * for traceability — it is never used to recompute {@code amount} when a
 * leading stated number is present.
 */
public record GoogleKeepLine(

        String rawLine,

        String categoryLabel,

        Money amount,

        String breakdownNotes

) {
}
