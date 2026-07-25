package io.rashed.finance.application.report;

import io.rashed.finance.application.account.CalculateAccountBalanceService;
import io.rashed.finance.application.fund.CalculateFundBalanceService;
import io.rashed.finance.application.loan.CalculateLoanBalanceService;
import io.rashed.finance.application.recurring.ListDueRecurringTransactionsService;
import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.categories.Category;
import io.rashed.finance.domain.categories.CategoryRepository;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundRepository;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetDashboardServiceTest {

    private final SalaryCycleId salaryCycleId = SalaryCycleId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private AccountRepository accountRepository;
    private CategoryRepository categoryRepository;
    private FundRepository fundRepository;
    private LoanRepository loanRepository;
    private TransactionRepository transactionRepository;
    private CalculateAccountBalanceService calculateAccountBalanceService;
    private CalculateFundBalanceService calculateFundBalanceService;
    private CalculateLoanBalanceService calculateLoanBalanceService;
    private ListDueRecurringTransactionsService listDueRecurringTransactionsService;
    private GetDashboardService service;

    @BeforeEach
    void setUp() {

        accountRepository = mock(AccountRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        fundRepository = mock(FundRepository.class);
        loanRepository = mock(LoanRepository.class);
        transactionRepository = mock(TransactionRepository.class);
        calculateAccountBalanceService = mock(CalculateAccountBalanceService.class);
        calculateFundBalanceService = mock(CalculateFundBalanceService.class);
        calculateLoanBalanceService = mock(CalculateLoanBalanceService.class);
        listDueRecurringTransactionsService = mock(ListDueRecurringTransactionsService.class);

        service = new GetDashboardService(
                accountRepository, categoryRepository, fundRepository, loanRepository, transactionRepository,
                calculateAccountBalanceService, calculateFundBalanceService, calculateLoanBalanceService,
                listDueRecurringTransactionsService);
    }

    @Test
    void execute_assemblesDashboardFromLedgerDerivation() {

        Account cash = Account.createCashAccount("Wallet", Money.zero());
        Account bank = Account.createBankAccount("Bank", Money.zero());
        when(accountRepository.findActive()).thenReturn(List.of(cash, bank));
        when(calculateAccountBalanceService.execute(cash.getId(), today)).thenReturn(Money.of(1000));
        when(calculateAccountBalanceService.execute(bank.getId(), today)).thenReturn(Money.of(5000));

        Fund fund = Fund.create("Emergency", FundType.EMERGENCY, null, null, null);
        when(fundRepository.findActive()).thenReturn(List.of(fund));
        when(calculateFundBalanceService.execute(fund.getId())).thenReturn(Money.of(2000));

        Loan loan = Loan.receivable("Friend Loan", Money.of(1000), today, null, null);
        when(loanRepository.findActiveLoans()).thenReturn(List.of(loan));
        when(calculateLoanBalanceService.execute(loan.getId())).thenReturn(Money.of(500));

        Category categoryA = Category.userExpenseCategory("Transport", null);
        Category categoryB = Category.userExpenseCategory("Groceries", null);
        when(categoryRepository.findById(categoryA.getId())).thenReturn(Optional.of(categoryA));
        when(categoryRepository.findById(categoryB.getId())).thenReturn(Optional.of(categoryB));

        Transaction income = Transaction.income(
                TransactionId.newId(), today, Money.of(5000), bank.getId(), categoryA.getId(), salaryCycleId, null);
        Transaction expenseA = Transaction.expense(
                TransactionId.newId(), today, Money.of(300), bank.getId(), categoryA.getId(), salaryCycleId, null);
        Transaction expenseB = Transaction.expense(
                TransactionId.newId(), today, Money.of(700), bank.getId(), categoryB.getId(), salaryCycleId, null);

        when(transactionRepository.find(any(), any(Pageable.class))).thenAnswer(invocation -> {

            TransactionFilter filter = invocation.getArgument(0);
            Pageable pageable = invocation.getArgument(1);

            if (pageable.isPaged()) {
                return new PageImpl<>(List.of(income, expenseB, expenseA));
            }
            if (filter.transactionType() == TransactionType.INCOME) {
                return new PageImpl<>(List.of(income));
            }
            return new PageImpl<>(List.of(expenseA, expenseB));
        });

        when(listDueRecurringTransactionsService.execute(today)).thenReturn(List.of());

        DashboardResult result = service.execute(today);

        assertEquals(Money.of(6000), result.totalBalance());
        assertEquals(Money.of(1000), result.cashBalance());
        assertEquals(Money.of(2000), result.totalFundBalance());
        assertEquals(Money.of(500), result.loanSummary().totalReceivable());
        assertEquals(Money.zero(), result.loanSummary().totalPayable());
        assertEquals(1, result.loanSummary().activeLoanCount());
        assertEquals(Money.of(5000), result.monthlyIncome());
        assertEquals(Money.of(1000), result.monthlyExpense());
        assertEquals(3, result.recentTransactions().size());
        assertEquals(2, result.topSpendingCategories().size());
        assertEquals(categoryB.getName(), result.topSpendingCategories().get(0).categoryName());
        assertEquals(Money.of(700), result.topSpendingCategories().get(0).totalSpent());
    }
}
