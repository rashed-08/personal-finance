package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.infrastructure.persistence.entity.SalaryCycleEntity;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryCycleJpaRepository extends JpaRepository<SalaryCycleEntity, UUID> {

    Optional<SalaryCycleEntity> findByCycleName(String cycleName);

    boolean existsByCycleName(String cycleName);

    Optional<SalaryCycleEntity> findByCycleStartDateLessThanEqualAndCycleEndDateGreaterThanEqual(
            LocalDate startDate, LocalDate endDate);
}
