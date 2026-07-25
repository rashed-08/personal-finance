package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;

/** Per docs/business/LoanWorkflow.md's Dashboard section. */
public record LoanSummary(

        Money totalReceivable,

        Money totalPayable,

        Money netPosition,

        long activeLoanCount

) {
}
