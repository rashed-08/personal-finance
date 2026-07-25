package io.rashed.finance.application.transaction.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

@Service
public class ListTransactionsService {


    private final TransactionRepository repository;


    public ListTransactionsService(
            TransactionRepository repository
    ) {
        this.repository = repository;
    }



    public Page<Transaction> execute(
            TransactionFilter filter,
            Pageable pageable
    ) {

        return repository.find(
                filter,
                pageable
        );
    }
}