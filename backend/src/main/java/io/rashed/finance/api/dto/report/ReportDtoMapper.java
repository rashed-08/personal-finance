package io.rashed.finance.api.dto.report;

import io.rashed.finance.api.dto.recurring.RecurringTransactionDtoMapper;
import io.rashed.finance.api.dto.transaction.TransactionDtoMapper;
import io.rashed.finance.application.report.AccountBalance;
import io.rashed.finance.application.report.AccountStatementResult;
import io.rashed.finance.application.report.CashFlowReportResult;
import io.rashed.finance.application.report.CategoryBreakdown;
import io.rashed.finance.application.report.CategoryReportResult;
import io.rashed.finance.application.report.CategorySpending;
import io.rashed.finance.application.report.DashboardResult;
import io.rashed.finance.application.report.DateBucket;
import io.rashed.finance.application.report.FundReportLine;
import io.rashed.finance.application.report.IncomeExpenseReportResult;
import io.rashed.finance.application.report.LoanPaymentHistoryLine;
import io.rashed.finance.application.report.LoanReportLine;
import io.rashed.finance.application.report.LoanSummary;
import io.rashed.finance.application.report.MonthComparison;
import io.rashed.finance.application.report.MonthlyAmount;
import io.rashed.finance.application.report.MonthlyReportResult;
import io.rashed.finance.application.report.RecurringTransactionReportLine;
import io.rashed.finance.application.report.SalaryCycleReportResult;
import io.rashed.finance.application.report.StatementLine;

import java.util.List;

public final class ReportDtoMapper {

    private ReportDtoMapper() {
    }

    public static DashboardResponse toResponse(DashboardResult result) {

        return new DashboardResponse(
                result.totalBalance().getAmount(),
                result.cashBalance().getAmount(),
                result.totalFundBalance().getAmount(),
                toResponse(result.loanSummary()),
                result.monthlyIncome().getAmount(),
                result.monthlyExpense().getAmount(),
                result.recentTransactions().stream().map(TransactionDtoMapper::toResponse).toList(),
                result.dueRecurringTransactions().stream().map(RecurringTransactionDtoMapper::toResponse).toList(),
                result.topSpendingCategories().stream().map(ReportDtoMapper::toResponse).toList()
        );
    }

    private static LoanSummaryResponse toResponse(LoanSummary summary) {

        return new LoanSummaryResponse(
                summary.totalReceivable().getAmount(),
                summary.totalPayable().getAmount(),
                summary.netPosition().getAmount(),
                summary.activeLoanCount()
        );
    }

    private static CategorySpendingResponse toResponse(CategorySpending spending) {

        return new CategorySpendingResponse(
                spending.categoryId().getValue(), spending.categoryName(), spending.totalSpent().getAmount());
    }

    public static IncomeExpenseReportResponse toResponse(IncomeExpenseReportResult result) {

        return new IncomeExpenseReportResponse(
                result.transactionType(),
                result.total().getAmount(),
                result.transactionCount(),
                result.byCategory().stream().map(ReportDtoMapper::toResponse).toList(),
                result.byDate().stream().map(ReportDtoMapper::toResponse).toList()
        );
    }

    private static CategoryBreakdownResponse toResponse(CategoryBreakdown breakdown) {

        return new CategoryBreakdownResponse(
                breakdown.categoryId().getValue(), breakdown.categoryName(), breakdown.total().getAmount(), breakdown.transactionCount());
    }

    private static DateBucketResponse toResponse(DateBucket bucket) {

        return new DateBucketResponse(bucket.date(), bucket.total().getAmount());
    }

    public static CategoryReportResponse toResponse(CategoryReportResult result) {

        return new CategoryReportResponse(
                result.categoryId().getValue(),
                result.categoryName(),
                result.totalSpending().getAmount(),
                result.transactionCount(),
                result.monthlyTrend().stream().map(ReportDtoMapper::toResponse).toList(),
                result.averagePerMonth().getAmount()
        );
    }

    private static MonthlyAmountResponse toResponse(MonthlyAmount amount) {

        return new MonthlyAmountResponse(amount.yearMonth(), amount.total().getAmount());
    }

    public static AccountBalanceResponse toResponse(AccountBalance balance) {

        return new AccountBalanceResponse(
                balance.accountId().getValue(), balance.accountName(), balance.accountType(), balance.balance().getAmount());
    }

    public static List<AccountBalanceResponse> toBalanceResponseList(List<AccountBalance> balances) {

        return balances.stream().map(ReportDtoMapper::toResponse).toList();
    }

    public static AccountStatementResponse toResponse(AccountStatementResult result) {

        return new AccountStatementResponse(
                result.accountId().getValue(),
                result.accountName(),
                result.openingBalance().getAmount(),
                result.lines().stream().map(ReportDtoMapper::toResponse).toList(),
                result.endingBalance().getAmount()
        );
    }

    private static StatementLineResponse toResponse(StatementLine line) {

        return new StatementLineResponse(
                line.transactionId().getValue(),
                line.transactionDate(),
                line.description(),
                line.transactionType(),
                line.signedAmount().getAmount(),
                line.runningBalance().getAmount()
        );
    }

    public static FundReportLineResponse toResponse(FundReportLine line) {

        return new FundReportLineResponse(
                line.fundId().getValue(),
                line.fundName(),
                line.fundType(),
                line.targetAmount() != null ? line.targetAmount().getAmount() : null,
                line.allocatedAmount().getAmount(),
                line.usedAmount().getAmount(),
                line.remainingBalance().getAmount(),
                line.progressPercentage()
        );
    }

    public static List<FundReportLineResponse> toFundResponseList(List<FundReportLine> lines) {

        return lines.stream().map(ReportDtoMapper::toResponse).toList();
    }

    public static LoanReportLineResponse toResponse(LoanReportLine line) {

        return new LoanReportLineResponse(
                line.loanId().getValue(),
                line.name(),
                line.loanType(),
                line.principalAmount().getAmount(),
                line.paidAmount().getAmount(),
                line.remainingAmount().getAmount(),
                line.loanStatus(),
                line.paymentHistory().stream().map(ReportDtoMapper::toResponse).toList()
        );
    }

    public static List<LoanReportLineResponse> toLoanResponseList(List<LoanReportLine> lines) {

        return lines.stream().map(ReportDtoMapper::toResponse).toList();
    }

    private static LoanPaymentHistoryLineResponse toResponse(LoanPaymentHistoryLine line) {

        return new LoanPaymentHistoryLineResponse(
                line.transactionId().getValue(), line.date(), line.amount().getAmount(), line.description());
    }

    public static SalaryCycleReportResponse toResponse(SalaryCycleReportResult result) {

        return new SalaryCycleReportResponse(
                result.salaryCycleId().getValue(),
                result.cycleName(),
                result.startDate(),
                result.endDate(),
                result.closed(),
                result.openingBalance().getAmount(),
                result.income().getAmount(),
                result.expenses().getAmount(),
                result.adjustments().getAmount(),
                result.closingBalance().getAmount()
        );
    }

    public static MonthlyReportResponse toResponse(MonthlyReportResult result) {

        return new MonthlyReportResponse(
                result.yearMonth(),
                result.totalIncome().getAmount(),
                result.totalExpense().getAmount(),
                result.netCashFlow().getAmount(),
                result.expenseByCategory().stream().map(ReportDtoMapper::toResponse).toList(),
                result.incomeByCategory().stream().map(ReportDtoMapper::toResponse).toList(),
                toResponse(result.comparisonToPreviousMonth())
        );
    }

    private static MonthComparisonResponse toResponse(MonthComparison comparison) {

        return new MonthComparisonResponse(
                comparison.currentIncome().getAmount(),
                comparison.previousIncome().getAmount(),
                comparison.currentExpense().getAmount(),
                comparison.previousExpense().getAmount()
        );
    }

    public static CashFlowReportResponse toResponse(CashFlowReportResult result) {

        return new CashFlowReportResponse(
                result.fromDate(),
                result.toDate(),
                result.moneyIn().getAmount(),
                result.moneyOut().getAmount(),
                result.netCashFlow().getAmount(),
                result.totalTransferVolume().getAmount()
        );
    }

    public static RecurringTransactionReportLineResponse toResponse(RecurringTransactionReportLine line) {

        return new RecurringTransactionReportLineResponse(
                line.recurringTransactionId().getValue(),
                line.name(),
                line.active(),
                line.nextExecutionDate(),
                line.lastExecutionDate(),
                line.generatedCount(),
                line.skippedCount()
        );
    }

    public static List<RecurringTransactionReportLineResponse> toRecurringResponseList(List<RecurringTransactionReportLine> lines) {

        return lines.stream().map(ReportDtoMapper::toResponse).toList();
    }
}
