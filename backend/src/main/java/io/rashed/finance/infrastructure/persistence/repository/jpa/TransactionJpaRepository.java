package io.rashed.finance.infrastructure.persistence.repository.jpa;


import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.infrastructure.persistence.entity.TransactionEntity;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface TransactionJpaRepository  extends JpaRepository<TransactionEntity, UUID>, JpaSpecificationExecutor<TransactionEntity> {

    boolean existsByTransactionTypeAndToAccountId(TransactionType transactionType, UUID toAccountId);

}