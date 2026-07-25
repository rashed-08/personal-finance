package io.rashed.finance.application.transaction.query;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;

@Service
public class GetTransactionService {

    private final TransactionRepository repository;

    public GetTransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction execute(TransactionId id) {

        Objects.requireNonNull(id);

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Transaction not found."));
    }
}