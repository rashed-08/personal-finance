package io.rashed.finance.application.loan;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanRepository;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;

/**
 * Records a repayment (payable loan) or collection (receivable loan) as a
 * loan-linked ledger transaction. See docs/business/LoanWorkflow.md — a
 * repayment must never exceed the outstanding balance.
 */
@Service
public class RecordRepaymentService {

    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final SalaryCycleRepository salaryCycleRepository;
    private final CalculateLoanBalanceService calculateLoanBalanceService;

    public RecordRepaymentService(
            LoanRepository loanRepository,
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            SalaryCycleRepository salaryCycleRepository,
            CalculateLoanBalanceService calculateLoanBalanceService
    ) {
        this.loanRepository = Objects.requireNonNull(loanRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.salaryCycleRepository = Objects.requireNonNull(salaryCycleRepository);
        this.calculateLoanBalanceService = Objects.requireNonNull(calculateLoanBalanceService);
    }

    public Loan execute(RecordRepaymentCommand command) {

        Objects.requireNonNull(command, "Command cannot be null.");

        Loan loan = loanRepository.findById(command.loanId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found."));

        if (!loan.isActive()) {
            throw new IllegalStateException("Repayments can only be recorded against an active loan.");
        }

        validateAccount(command.accountId());
        validateSalaryCycle(command.salaryCycleId());

        Money outstanding = calculateLoanBalanceService.execute(loan.getId());

        if (command.amount().greaterThan(outstanding)) {
            throw new TransactionValidationException(
                    "Repayment amount cannot exceed the outstanding balance of " + outstanding + ".");
        }

        AccountId fromAccountId = loan.isReceivable() ? null : command.accountId();
        AccountId toAccountId = loan.isReceivable() ? command.accountId() : null;

        Transaction repayment = Transaction.loanTransfer(
                TransactionId.newId(),
                command.paymentDate(),
                command.amount(),
                fromAccountId,
                toAccountId,
                loan.getId(),
                command.salaryCycleId(),
                command.description()
        );

        transactionRepository.save(repayment);

        return loan;
    }

    private void validateAccount(AccountId accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found."));

        if (account.isInactive()) {
            throw new TransactionValidationException("Account is not active.");
        }
    }

    private void validateSalaryCycle(SalaryCycleId salaryCycleId) {

        Objects.requireNonNull(salaryCycleId, "Repayment requires a salary cycle.");

        if (salaryCycleRepository.findById(salaryCycleId).isEmpty()) {
            throw new ResourceNotFoundException("Salary cycle not found.");
        }
    }
}
