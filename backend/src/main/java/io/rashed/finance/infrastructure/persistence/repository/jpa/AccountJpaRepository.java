package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<AccountEntity> findAllByOrderByNameAsc();

    List<AccountEntity> findByActiveTrueOrderByNameAsc();

    List<AccountEntity> findByActiveFalseOrderByNameAsc();

    List<AccountEntity> findByAccountTypeOrderByNameAsc(AccountType accountType);
}