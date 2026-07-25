package io.rashed.finance.application.report;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;

/** One calendar month's income/expense digest, including a comparison to the previous month. */
@Service
public class GetMonthlyReportService {

    private final GetIncomeExpenseReportService getIncomeExpenseReportService;

    public GetMonthlyReportService(GetIncomeExpenseReportService getIncomeExpenseReportService) {
        this.getIncomeExpenseReportService = Objects.requireNonNull(getIncomeExpenseReportService);
    }

    public MonthlyReportResult execute(YearMonth yearMonth) {

        Objects.requireNonNull(yearMonth, "Month cannot be null.");

        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        IncomeExpenseReportResult income = getIncomeExpenseReportService.execute(
                new IncomeExpenseReportQuery(TransactionType.INCOME, monthStart, monthEnd, null, null, null));

        IncomeExpenseReportResult expense = getIncomeExpenseReportService.execute(
                new IncomeExpenseReportQuery(TransactionType.EXPENSE, monthStart, monthEnd, null, null, null));

        YearMonth previousMonth = yearMonth.minusMonths(1);
        LocalDate previousStart = previousMonth.atDay(1);
        LocalDate previousEnd = previousMonth.atEndOfMonth();

        Money previousIncome = getIncomeExpenseReportService.execute(
                new IncomeExpenseReportQuery(TransactionType.INCOME, previousStart, previousEnd, null, null, null)).total();

        Money previousExpense = getIncomeExpenseReportService.execute(
                new IncomeExpenseReportQuery(TransactionType.EXPENSE, previousStart, previousEnd, null, null, null)).total();

        Money net = income.total().subtract(expense.total());

        MonthComparison comparison = new MonthComparison(income.total(), previousIncome, expense.total(), previousExpense);

        return new MonthlyReportResult(
                yearMonth, income.total(), expense.total(), net, expense.byCategory(), income.byCategory(), comparison);
    }
}
