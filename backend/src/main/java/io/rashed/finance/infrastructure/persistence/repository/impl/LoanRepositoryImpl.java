package io.rashed.finance.infrastructure.persistence.repository.impl;

import io.rashed.finance.common.enums.LoanStatus;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.domain.loans.LoanRepository;
import io.rashed.finance.infrastructure.persistence.mapper.LoanEntityMapper;
import io.rashed.finance.infrastructure.persistence.repository.jpa.LoanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoanRepositoryImpl implements LoanRepository {

    private final LoanJpaRepository jpaRepository;

    @Override
    public Loan save(Loan loan) {

        return LoanEntityMapper.toDomain(
                jpaRepository.save(
                        LoanEntityMapper.toEntity(loan)
                )
        );
    }

    @Override
    public Optional<Loan> findById(LoanId id) {

        return jpaRepository.findById(id.getValue())
                .map(LoanEntityMapper::toDomain);
    }

    @Override
    public List<Loan> findAll() {

        return jpaRepository.findAllByOrderByStartDateDesc()
                .stream()
                .map(LoanEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Loan> findActiveLoans() {

        return jpaRepository.findByLoanStatusOrderByStartDateDesc(LoanStatus.ACTIVE)
                .stream()
                .map(LoanEntityMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(LoanId id) {

        return jpaRepository.existsById(id.getValue());
    }
}
