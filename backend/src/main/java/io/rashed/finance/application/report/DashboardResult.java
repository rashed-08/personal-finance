package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.transactions.Transaction;

import java.util.List;

public record DashboardResult(

        Money totalBalance,

        Money cashBalance,

        Money totalFundBalance,

        LoanSummary loanSummary,

        Money monthlyIncome,

        Money monthlyExpense,

        List<Transaction> recentTransactions,

        List<RecurringTransaction> dueRecurringTransactions,

        List<CategorySpending> topSpendingCategories

) {
}
