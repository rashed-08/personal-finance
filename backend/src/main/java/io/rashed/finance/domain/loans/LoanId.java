package io.rashed.finance.domain.loans;

import io.rashed.finance.common.valueobject.EntityId;
import java.util.UUID;

/**
 * Strongly typed identifier for Loan aggregate.
 */
public final class LoanId extends EntityId {

    private LoanId(UUID value) {
        super(value);
    }

    public static LoanId newId() {
        return new LoanId(UUID.randomUUID());
    }

    public static LoanId of(UUID value) {
        return new LoanId(value);
    }

    public static LoanId of(String value) {
        return new LoanId(UUID.fromString(value));
    }
}