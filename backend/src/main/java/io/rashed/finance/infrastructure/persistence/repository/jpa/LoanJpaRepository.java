package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.common.enums.LoanStatus;
import io.rashed.finance.infrastructure.persistence.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanJpaRepository extends JpaRepository<LoanEntity, UUID> {

    List<LoanEntity> findAllByOrderByStartDateDesc();

    List<LoanEntity> findByLoanStatusOrderByStartDateDesc(LoanStatus loanStatus);
}
