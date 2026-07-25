package io.rashed.finance.application.report;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import io.rashed.finance.application.account.CalculateAccountBalanceService;
import io.rashed.finance.application.fund.CalculateFundBalanceService;
import io.rashed.finance.application.loan.CalculateLoanBalanceService;
import io.rashed.finance.application.recurring.ListDueRecurringTransactionsService;
import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundRepository;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

/**
 * Assembles the dashboard entirely from live ledger derivation — nothing
 * here is stored. See docs/database/tables/transactions/07-reporting.md and
 * docs/requirements/FunctionalRequirements.md FR-012.
 */
@Service
public class GetDashboardService {

    private static final int RECENT_TRANSACTIONS_LIMIT = 10;
    private static final int TOP_CATEGORIES_LIMIT = 5;

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final FundRepository fundRepository;
    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final CalculateAccountBalanceService calculateAccountBalanceService;
    private final CalculateFundBalanceService calculateFundBalanceService;
    private final CalculateLoanBalanceService calculateLoanBalanceService;
    private final ListDueRecurringTransactionsService listDueRecurringTransactionsService;

    public GetDashboardService(
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            FundRepository fundRepository,
            LoanRepository loanRepository,
            TransactionRepository transactionRepository,
            CalculateAccountBalanceService calculateAccountBalanceService,
            CalculateFundBalanceService calculateFundBalanceService,
            CalculateLoanBalanceService calculateLoanBalanceService,
            ListDueRecurringTransactionsService listDueRecurringTransactionsService
    ) {
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.categoryRepository = Objects.requireNonNull(categoryRepository);
        this.fundRepository = Objects.requireNonNull(fundRepository);
        this.loanRepository = Objects.requireNonNull(loanRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.calculateAccountBalanceService = Objects.requireNonNull(calculateAccountBalanceService);
        this.calculateFundBalanceService = Objects.requireNonNull(calculateFundBalanceService);
        this.calculateLoanBalanceService = Objects.requireNonNull(calculateLoanBalanceService);
        this.listDueRecurringTransactionsService = Objects.requireNonNull(listDueRecurringTransactionsService);
    }

    public DashboardResult execute(LocalDate asOfDate) {

        Objects.requireNonNull(asOfDate, "Date cannot be null.");

        Money totalBalance = Money.zero();
        Money cashBalance = Money.zero();

        for (Account account : accountRepository.findActive()) {

            Money balance = calculateAccountBalanceService.execute(account.getId(), asOfDate);
            totalBalance = totalBalance.add(balance);

            if (account.isCashAccount()) {
                cashBalance = cashBalance.add(balance);
            }
        }

        Money totalFundBalance = Money.zero();

        for (Fund fund : fundRepository.findActive()) {
            totalFundBalance = totalFundBalance.add(calculateFundBalanceService.execute(fund.getId()));
        }

        LocalDate monthStart = asOfDate.withDayOfMonth(1);
        LocalDate monthEnd = asOfDate.withDayOfMonth(asOfDate.lengthOfMonth());

        return new DashboardResult(
                totalBalance,
                cashBalance,
                totalFundBalance,
                calculateLoanSummary(),
                sumByType(TransactionType.INCOME, monthStart, monthEnd),
                sumByType(TransactionType.EXPENSE, monthStart, monthEnd),
                recentTransactions(),
                listDueRecurringTransactionsService.execute(asOfDate),
                topSpendingCategories(monthStart, monthEnd)
        );
    }

    private LoanSummary calculateLoanSummary() {

        Money totalReceivable = Money.zero();
        Money totalPayable = Money.zero();
        long activeLoanCount = 0;

        for (Loan loan : loanRepository.findActiveLoans()) {

            Money outstanding = calculateLoanBalanceService.execute(loan.getId());
            activeLoanCount++;

            if (loan.isReceivable()) {
                totalReceivable = totalReceivable.add(outstanding);
            } else {
                totalPayable = totalPayable.add(outstanding);
            }
        }

        return new LoanSummary(totalReceivable, totalPayable, totalReceivable.subtract(totalPayable), activeLoanCount);
    }

    private Money sumByType(TransactionType type, LocalDate fromDate, LocalDate toDate) {

        TransactionFilter filter = new TransactionFilter(
                fromDate, toDate, type, TransactionStatus.POSTED, null, null, null, null, null);

        Money total = Money.zero();

        for (Transaction transaction : transactionRepository.find(filter, Pageable.unpaged())) {
            total = total.add(transaction.getAmount());
        }

        return total;
    }

    private List<Transaction> recentTransactions() {

        TransactionFilter filter = new TransactionFilter(
                null, null, null, TransactionStatus.POSTED, null, null, null, null, null);

        Pageable pageable = PageRequest.of(0, RECENT_TRANSACTIONS_LIMIT, Sort.by(Sort.Direction.DESC, "transactionDate"));

        return transactionRepository.find(filter, pageable).getContent();
    }

    private List<CategorySpending> topSpendingCategories(LocalDate fromDate, LocalDate toDate) {

        TransactionFilter filter = new TransactionFilter(
                fromDate, toDate, TransactionType.EXPENSE, TransactionStatus.POSTED, null, null, null, null, null);

        Map<CategoryId, Money> totals = new LinkedHashMap<>();

        for (Transaction transaction : transactionRepository.find(filter, Pageable.unpaged())) {

            if (!transaction.hasCategory()) {
                continue;
            }

            totals.merge(transaction.getCategoryId(), transaction.getAmount(), Money::add);
        }

        return totals.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(TOP_CATEGORIES_LIMIT)
                .map(entry -> new CategorySpending(
                        entry.getKey(),
                        categoryRepository.findById(entry.getKey()).map(Category::getName).orElse("Unknown"),
                        entry.getValue()
                ))
                .toList();
    }
}
