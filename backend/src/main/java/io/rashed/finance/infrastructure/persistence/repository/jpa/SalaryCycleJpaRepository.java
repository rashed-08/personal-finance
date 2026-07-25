package io.rashed.finance.infrastructure.persistence.repository.jpa;

import io.rashed.finance.infrastructure.persistence.entity.SalaryCycleEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SalaryCycleJpaRepository extends JpaRepository<SalaryCycleEntity, UUID> {

    Optional<SalaryCycleEntity> findByCycleName(String cycleName);

    boolean existsByCycleName(String cycleName);

    @Query("""
            SELECT c FROM SalaryCycleEntity c
            WHERE c.cycleStartDate <= :date
              AND (c.cycleEndDate IS NULL OR c.cycleEndDate >= :date)
            """)
    Optional<SalaryCycleEntity> findContaining(LocalDate date);

    Optional<SalaryCycleEntity> findByCycleEndDateIsNull();

    @Query("""
            SELECT c FROM SalaryCycleEntity c
            WHERE c.cycleStartDate < :startDate
            ORDER BY c.cycleStartDate DESC
            """)
    List<SalaryCycleEntity> findPrevious(LocalDate startDate, Pageable limit);
}
