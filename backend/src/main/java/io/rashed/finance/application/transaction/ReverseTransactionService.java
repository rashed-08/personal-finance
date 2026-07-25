package io.rashed.finance.application.transaction;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class ReverseTransactionService {

    private final TransactionRepository repository;

    public ReverseTransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction execute(TransactionId id) {

        Transaction transaction = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transaction not found."));

        Transaction reversed = transaction.reverse();

        return repository.save(reversed);
    }
}
