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

    /**
     * Every cycle covering the date, most recently started first.
     *
     * <p>Returns a list rather than an {@code Optional} because nothing
     * enforces that at most one cycle covers a date: salary_cycles has no
     * exclusion constraint, and an open cycle (cycle_end_date IS NULL)
     * covers every date after its start, so it overlaps every cycle opened
     * later. An {@code Optional} return made Hibernate throw
     * NonUniqueResultException ("Query did not return a unique result") on
     * such ledgers, which surfaced as a 500 — notably from the Google Keep
     * import, which resolves a cycle per imported month.
     *
     * <p>Callers wanting the single best match take the first element: the
     * latest start is the most specific cycle for that date.
     */
    @Query("""
            SELECT c FROM SalaryCycleEntity c
            WHERE c.cycleStartDate <= :date
              AND (c.cycleEndDate IS NULL OR c.cycleEndDate >= :date)
            ORDER BY c.cycleStartDate DESC, c.id ASC
            """)
    List<SalaryCycleEntity> findContaining(LocalDate date);

    Optional<SalaryCycleEntity> findByCycleEndDateIsNull();

    @Query("""
            SELECT c FROM SalaryCycleEntity c
            WHERE c.cycleStartDate < :startDate
            ORDER BY c.cycleStartDate DESC
            """)
    List<SalaryCycleEntity> findPrevious(LocalDate startDate, Pageable limit);
}
