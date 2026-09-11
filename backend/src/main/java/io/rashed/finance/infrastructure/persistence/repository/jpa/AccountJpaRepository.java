package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    /**
     * Accounts with this name, oldest first. A list, not an
     * {@code Optional}: accounts.name has no unique index, so two accounts
     * can share a name (differing only in case is enough) and an
     * {@code Optional} return would throw NonUniqueResultException instead
     * of answering. Callers take the first — the original row.
     */
    List<AccountEntity> findByNameIgnoreCaseOrderByCreatedAtAsc(String name);

    boolean existsByNameIgnoreCase(String name);

    List<AccountEntity> findAllByOrderByNameAsc();

    List<AccountEntity> findByActiveTrueOrderByNameAsc();

    List<AccountEntity> findByActiveFalseOrderByNameAsc();

    List<AccountEntity> findByAccountTypeOrderByNameAsc(AccountType accountType);
}