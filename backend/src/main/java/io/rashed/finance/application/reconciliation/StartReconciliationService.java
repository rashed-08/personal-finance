package io.rashed.finance.application.reconciliation;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.application.account.CalculateAccountBalanceService;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.exception.TransactionValidationException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountRepository;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;

@Service
public class StartReconciliationService {

    private final CashReconciliationRepository repository;
    private final AccountRepository accountRepository;
    private final CalculateAccountBalanceService calculateAccountBalanceService;

    public StartReconciliationService(
            CashReconciliationRepository repository,
            AccountRepository accountRepository,
            CalculateAccountBalanceService calculateAccountBalanceService
    ) {
        this.repository = Objects.requireNonNull(repository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
        this.calculateAccountBalanceService = Objects.requireNonNull(calculateAccountBalanceService);
    }

    public CashReconciliation execute(StartReconciliationCommand command) {

        Objects.requireNonNull(command, "Command cannot be null.");

        Account account = accountRepository.findById(command.accountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found."));

        if (account.isInactive()) {
            throw new TransactionValidationException("Account is not active.");
        }

        if (!account.isCashAccount()) {
            throw new TransactionValidationException("Only cash accounts can be reconciled.");
        }

        if (repository.existsPendingForAccount(command.accountId())) {
            throw new IllegalStateException("This account already has a reconciliation in progress.");
        }

        Money expectedCash = calculateAccountBalanceService.execute(
                command.accountId(), command.reconciliationDate());

        CashReconciliation reconciliation = CashReconciliation.start(
                command.accountId(), command.reconciliationDate(), expectedCash, command.notes());

        return repository.save(reconciliation);
    }
}
