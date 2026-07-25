package io.rashed.finance.application.loan;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.LoanType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
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
 * Creating a loan also posts its disbursement/receipt as a loan-linked
 * ledger transaction, per docs/business/LoanWorkflow.md's worked example
 * where giving/receiving a loan immediately changes the account's
 * available balance.
 */
@Service
public class CreateLoanService {

    private final LoanRepository loanRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final SalaryCycleRepository salaryCycleRepository;

    public CreateLoanService(
            LoanRepository loanRepository,
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            SalaryCycleRepository salaryCycleRepository
    ) {
        this.loanRepository = Objects.requireNonNull(loanRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.salaryCycleRepository = Objects.requireNonNull(salaryCycleRepository);
    }

    public Loan create(CreateLoanCommand command) {

        Objects.requireNonNull(command);

        validateAccount(command.accountId());
        validateSalaryCycle(command.salaryCycleId());

        Loan loan = command.loanType() == LoanType.RECEIVABLE
                ? Loan.receivable(
                        command.name(),
                        command.principalAmount(),
                        command.startDate(),
                        command.dueDate(),
                        command.description())
                : Loan.payable(
                        command.name(),
                        command.principalAmount(),
                        command.startDate(),
                        command.dueDate(),
                        command.description());

        Loan saved = loanRepository.save(loan);

        // Receivable: money leaves the account (given to the borrower).
        // Payable: money enters the account (received from the lender).
        AccountId fromAccountId = saved.isReceivable() ? command.accountId() : null;
        AccountId toAccountId = saved.isReceivable() ? null : command.accountId();

        Transaction disbursement = Transaction.loanTransfer(
                TransactionId.newId(),
                command.startDate(),
                command.principalAmount(),
                fromAccountId,
                toAccountId,
                saved.getId(),
                command.salaryCycleId(),
                command.description()
        );

        transactionRepository.save(disbursement);

        return saved;
    }

    private void validateAccount(AccountId accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found."));

        if (account.isInactive()) {
            throw new TransactionValidationException("Account is not active.");
        }
    }

    private void validateSalaryCycle(SalaryCycleId salaryCycleId) {

        Objects.requireNonNull(salaryCycleId, "Loan requires a salary cycle.");

        if (salaryCycleRepository.findById(salaryCycleId).isEmpty()) {
            throw new ResourceNotFoundException("Salary cycle not found.");
        }
    }
}
