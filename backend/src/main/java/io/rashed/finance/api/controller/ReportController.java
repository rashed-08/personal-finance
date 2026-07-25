package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.reconciliation.CashReconciliationDtoMapper;
import io.rashed.finance.api.dto.reconciliation.CashReconciliationResponse;
import io.rashed.finance.api.dto.report.AccountBalanceResponse;
import io.rashed.finance.api.dto.report.AccountStatementResponse;
import io.rashed.finance.api.dto.report.CashFlowReportResponse;
import io.rashed.finance.api.dto.report.CategoryReportResponse;
import io.rashed.finance.api.dto.report.DashboardResponse;
import io.rashed.finance.api.dto.report.FundReportLineResponse;
import io.rashed.finance.api.dto.report.IncomeExpenseReportResponse;
import io.rashed.finance.api.dto.report.LoanReportLineResponse;
import io.rashed.finance.api.dto.report.MonthlyReportResponse;
import io.rashed.finance.api.dto.report.RecurringTransactionReportLineResponse;
import io.rashed.finance.api.dto.report.ReportDtoMapper;
import io.rashed.finance.api.dto.report.SalaryCycleReportResponse;
import io.rashed.finance.application.report.CategoryReportQuery;
import io.rashed.finance.application.report.GetAccountBalancesReportService;
import io.rashed.finance.application.report.GetAccountStatementService;
import io.rashed.finance.application.report.GetCashFlowReportService;
import io.rashed.finance.application.report.GetCashReconciliationReportService;
import io.rashed.finance.application.report.GetCategoryReportService;
import io.rashed.finance.application.report.GetDashboardService;
import io.rashed.finance.application.report.GetFundReportService;
import io.rashed.finance.application.report.GetIncomeExpenseReportService;
import io.rashed.finance.application.report.GetLoanReportService;
import io.rashed.finance.application.report.GetMonthlyReportService;
import io.rashed.finance.application.report.GetRecurringTransactionReportService;
import io.rashed.finance.application.report.GetSalaryCycleReportService;
import io.rashed.finance.application.report.IncomeExpenseReportQuery;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Every endpoint here is read-only and derives its figures live from the
 * ledger — nothing is stored. See docs/database/tables/transactions/07-reporting.md.
 */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final GetDashboardService getDashboardService;
    private final GetIncomeExpenseReportService getIncomeExpenseReportService;
    private final GetCategoryReportService getCategoryReportService;
    private final GetAccountBalancesReportService getAccountBalancesReportService;
    private final GetAccountStatementService getAccountStatementService;
    private final GetFundReportService getFundReportService;
    private final GetLoanReportService getLoanReportService;
    private final GetSalaryCycleReportService getSalaryCycleReportService;
    private final GetMonthlyReportService getMonthlyReportService;
    private final GetCashFlowReportService getCashFlowReportService;
    private final GetCashReconciliationReportService getCashReconciliationReportService;
    private final GetRecurringTransactionReportService getRecurringTransactionReportService;

    public ReportController(
            GetDashboardService getDashboardService,
            GetIncomeExpenseReportService getIncomeExpenseReportService,
            GetCategoryReportService getCategoryReportService,
            GetAccountBalancesReportService getAccountBalancesReportService,
            GetAccountStatementService getAccountStatementService,
            GetFundReportService getFundReportService,
            GetLoanReportService getLoanReportService,
            GetSalaryCycleReportService getSalaryCycleReportService,
            GetMonthlyReportService getMonthlyReportService,
            GetCashFlowReportService getCashFlowReportService,
            GetCashReconciliationReportService getCashReconciliationReportService,
            GetRecurringTransactionReportService getRecurringTransactionReportService
    ) {
        this.getDashboardService = getDashboardService;
        this.getIncomeExpenseReportService = getIncomeExpenseReportService;
        this.getCategoryReportService = getCategoryReportService;
        this.getAccountBalancesReportService = getAccountBalancesReportService;
        this.getAccountStatementService = getAccountStatementService;
        this.getFundReportService = getFundReportService;
        this.getLoanReportService = getLoanReportService;
        this.getSalaryCycleReportService = getSalaryCycleReportService;
        this.getMonthlyReportService = getMonthlyReportService;
        this.getCashFlowReportService = getCashFlowReportService;
        this.getCashReconciliationReportService = getCashReconciliationReportService;
        this.getRecurringTransactionReportService = getRecurringTransactionReportService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        return ReportDtoMapper.toResponse(getDashboardService.execute(asOfDate != null ? asOfDate : LocalDate.now()));
    }

    @GetMapping("/income")
    public IncomeExpenseReportResponse income(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID salaryCycleId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID categoryId) {

        return ReportDtoMapper.toResponse(getIncomeExpenseReportService.execute(new IncomeExpenseReportQuery(
                TransactionType.INCOME, fromDate, toDate,
                salaryCycleId == null ? null : SalaryCycleId.of(salaryCycleId),
                accountId == null ? null : AccountId.of(accountId),
                categoryId == null ? null : CategoryId.of(categoryId))));
    }

    @GetMapping("/expense")
    public IncomeExpenseReportResponse expense(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) UUID salaryCycleId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID categoryId) {

        return ReportDtoMapper.toResponse(getIncomeExpenseReportService.execute(new IncomeExpenseReportQuery(
                TransactionType.EXPENSE, fromDate, toDate,
                salaryCycleId == null ? null : SalaryCycleId.of(salaryCycleId),
                accountId == null ? null : AccountId.of(accountId),
                categoryId == null ? null : CategoryId.of(categoryId))));
    }

    @GetMapping("/category")
    public CategoryReportResponse category(
            @RequestParam UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return ReportDtoMapper.toResponse(getCategoryReportService.execute(
                new CategoryReportQuery(CategoryId.of(categoryId), fromDate, toDate)));
    }

    @GetMapping("/accounts/balances")
    public List<AccountBalanceResponse> accountBalances(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            @RequestParam(defaultValue = "true") boolean activeOnly) {

        return ReportDtoMapper.toBalanceResponseList(
                getAccountBalancesReportService.execute(asOfDate != null ? asOfDate : LocalDate.now(), activeOnly));
    }

    @GetMapping("/accounts/{id}/statement")
    public AccountStatementResponse accountStatement(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return ReportDtoMapper.toResponse(getAccountStatementService.execute(AccountId.of(id), fromDate, toDate));
    }

    @GetMapping("/funds")
    public List<FundReportLineResponse> funds(@RequestParam(defaultValue = "true") boolean activeOnly) {

        return ReportDtoMapper.toFundResponseList(getFundReportService.execute(activeOnly));
    }

    @GetMapping("/funds/{id}")
    public FundReportLineResponse fund(@PathVariable UUID id) {

        return ReportDtoMapper.toResponse(getFundReportService.executeOne(FundId.of(id)));
    }

    @GetMapping("/loans")
    public List<LoanReportLineResponse> loans(@RequestParam(defaultValue = "true") boolean activeOnly) {

        return ReportDtoMapper.toLoanResponseList(getLoanReportService.execute(activeOnly));
    }

    @GetMapping("/loans/{id}")
    public LoanReportLineResponse loan(@PathVariable UUID id) {

        return ReportDtoMapper.toResponse(getLoanReportService.executeOne(LoanId.of(id)));
    }

    @GetMapping("/salary-cycles/{id}")
    public SalaryCycleReportResponse salaryCycle(@PathVariable UUID id) {

        return ReportDtoMapper.toResponse(getSalaryCycleReportService.execute(SalaryCycleId.of(id)));
    }

    @GetMapping("/monthly")
    public MonthlyReportResponse monthly(@RequestParam(required = false) String yearMonth) {

        YearMonth resolved = yearMonth != null ? YearMonth.parse(yearMonth) : YearMonth.now();

        return ReportDtoMapper.toResponse(getMonthlyReportService.execute(resolved));
    }

    @GetMapping("/cash-flow")
    public CashFlowReportResponse cashFlow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return ReportDtoMapper.toResponse(getCashFlowReportService.execute(fromDate, toDate));
    }

    @GetMapping("/cash-reconciliation")
    public List<CashReconciliationResponse> cashReconciliation(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return CashReconciliationDtoMapper.toResponseList(getCashReconciliationReportService.execute(
                accountId == null ? null : AccountId.of(accountId), fromDate, toDate));
    }

    @GetMapping("/recurring-transactions")
    public List<RecurringTransactionReportLineResponse> recurringTransactions(
            @RequestParam(defaultValue = "true") boolean activeOnly) {

        return ReportDtoMapper.toRecurringResponseList(getRecurringTransactionReportService.execute(activeOnly));
    }
}
