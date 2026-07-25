package io.rashed.finance.application.loan;

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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Outstanding balance = principal amount - total repayments (docs/database/tables/loans.md).
 * The disbursement/receipt transaction moves money in the opposite direction from
 * repayments/collections, so it is naturally excluded from the sum without needing
 * to track transaction order.
 */
@Service
public class CalculateLoanBalanceService {

    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;

    public CalculateLoanBalanceService(LoanRepository loanRepository, TransactionRepository transactionRepository) {
        this.loanRepository = Objects.requireNonNull(loanRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public Money execute(LoanId loanId) {

        Objects.requireNonNull(loanId, "Loan cannot be null.");

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found."));

        TransactionFilter filter = new TransactionFilter(
                null, null, TransactionType.TRANSFER, TransactionStatus.POSTED, null, null, null, null, loanId, null);

        Money totalRepayments = Money.zero();

        for (Transaction transaction : transactionRepository.find(filter, Pageable.unpaged())) {

            boolean isRepayment = loan.isReceivable() ? transaction.hasToAccount() : transaction.hasFromAccount();

            if (isRepayment) {
                totalRepayments = totalRepayments.add(transaction.getAmount());
            }
        }

        return loan.getPrincipalAmount().subtract(totalRepayments);
    }
}
