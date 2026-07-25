package io.rashed.finance.domain.salarycycle;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalaryCycleRepository {

    SalaryCycle save(SalaryCycle salaryCycle);

    Optional<SalaryCycle> findById(SalaryCycleId id);

    Optional<SalaryCycle> findCurrent();

    Optional<SalaryCycle> findByName(String name);

    Optional<SalaryCycle> findByDate(LocalDate date);

    /** The one salary cycle without an end date yet, if any. */
    Optional<SalaryCycle> findOpen();

    /** The most recent cycle that starts strictly before the given date. */
    Optional<SalaryCycle> findPrevious(LocalDate beforeStartDate);

    List<SalaryCycle> findAll();

    boolean existsByName(String name);

    void delete(SalaryCycleId id);
}