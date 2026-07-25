package io.rashed.finance.infrastructure.persistence.repository.impl;


import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;


import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;

import io.rashed.finance.infrastructure.persistence.mapper.TransactionEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.TransactionJpaRepository;
import io.rashed.finance.infrastructure.persistence.specification.TransactionSpecification;



@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl 
        implements TransactionRepository {



    private final TransactionJpaRepository jpaRepository;



    @Override
    public Transaction save(Transaction transaction) {


        return TransactionEntityMapper.toDomain(
                jpaRepository.save(
                        TransactionEntityMapper.toEntity(transaction)
                )
        );
    }



    @Override
    public Optional<Transaction> findById(
            TransactionId id
    ) {


        return jpaRepository.findById(id.getValue())
                .map(TransactionEntityMapper::toDomain);

    }



    @Override
    public List<Transaction> findAll() {


        return jpaRepository.findAll()
                .stream()
                .map(TransactionEntityMapper::toDomain)
                .toList();

    }




    @Override
    public Page<Transaction> find(TransactionFilter filter, Pageable pageable) {

        return jpaRepository.findAll(TransactionSpecification.withFilter(filter), pageable)
                .map(TransactionEntityMapper::toDomain);
    }
}