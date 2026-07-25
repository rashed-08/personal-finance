package io.rashed.finance.api.dto.report;

import io.rashed.finance.api.dto.recurring.RecurringTransactionResponse;
import io.rashed.finance.api.dto.transaction.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(

        BigDecimal totalBalance,

        BigDecimal cashBalance,

        BigDecimal totalFundBalance,

        LoanSummaryResponse loanSummary,

        BigDecimal monthlyIncome,

        BigDecimal monthlyExpense,

        List<TransactionResponse> recentTransactions,

        List<RecurringTransactionResponse> dueRecurringTransactions,

        List<CategorySpendingResponse> topSpendingCategories

) {
}
