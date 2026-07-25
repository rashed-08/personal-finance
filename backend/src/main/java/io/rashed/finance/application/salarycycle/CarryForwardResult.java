package io.rashed.finance.application.salarycycle;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

/**
 * Opening Balance + Income - Expenses +/- Adjustments = Closing Balance,
 * per docs/business/CarryForwardWorkflow.md. Transfers are excluded because
 * they move money between accounts without changing net worth. Funds, Loans
 * and Cash Reconciliation are not yet reflected here because those modules
 * don't exist yet; once they do, they participate through ordinary ledger
 * transactions and this calculation picks them up automatically.
 */
public record CarryForwardResult(

        SalaryCycleId salaryCycleId,

        Money openingBalance,

        Money income,

        Money expenses,

        Money adjustments,

        Money closingBalance

) {
}
