package io.rashed.finance.application.report;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.rashed.finance.application.loan.CalculateLoanBalanceService;
import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.domain.loans.LoanRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

/**
 * Original/Paid/Remaining/Payment history per docs/database/tables/loans.md
 * and docs/database/tables/transactions/07-reporting.md 5.4. Supports both
 * RECEIVABLE and PAYABLE loans.
 */
@Service
public class GetLoanReportService {

    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final CalculateLoanBalanceService calculateLoanBalanceService;

    public GetLoanReportService(
            LoanRepository loanRepository,
            TransactionRepository transactionRepository,
            CalculateLoanBalanceService calculateLoanBalanceService
    ) {
        this.loanRepository = Objects.requireNonNull(loanRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.calculateLoanBalanceService = Objects.requireNonNull(calculateLoanBalanceService);
    }

    public List<LoanReportLine> execute(boolean activeOnly) {

        List<Loan> loans = activeOnly ? loanRepository.findActiveLoans() : loanRepository.findAll();

        return loans.stream().map(this::toLine).toList();
    }

    public LoanReportLine executeOne(LoanId loanId) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found."));

        return toLine(loan);
    }

    private LoanReportLine toLine(Loan loan) {

        TransactionFilter filter = new TransactionFilter(
                null, null, TransactionType.TRANSFER, TransactionStatus.POSTED, null, null, null, null, loan.getId(), null);

        List<LoanPaymentHistoryLine> paymentHistory = new ArrayList<>();

        for (Transaction transaction : transactionRepository.find(filter, Pageable.unpaged())) {

            boolean isRepayment = loan.isReceivable() ? transaction.hasToAccount() : transaction.hasFromAccount();

            if (isRepayment) {
                paymentHistory.add(new LoanPaymentHistoryLine(
                        transaction.getId(), transaction.getTransactionDate(), transaction.getAmount(), transaction.getDescription()));
            }
        }

        Money remaining = calculateLoanBalanceService.execute(loan.getId());
        Money paid = loan.getPrincipalAmount().subtract(remaining);

        return new LoanReportLine(
                loan.getId(), loan.getName(), loan.getLoanType(), loan.getPrincipalAmount(),
                paid, remaining, loan.getLoanStatus(), paymentHistory);
    }
}
