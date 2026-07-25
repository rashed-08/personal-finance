package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.infrastructure.persistence.entity.FundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FundJpaRepository extends JpaRepository<FundEntity, UUID> {

    Optional<FundEntity> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<FundEntity> findAllByOrderByNameAsc();

    List<FundEntity> findByActiveTrueOrderByNameAsc();

    List<FundEntity> findByFundTypeOrderByNameAsc(FundType fundType);
}
