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

    List<SalaryCycle> findAll();

    boolean existsByName(String name);

    void delete(SalaryCycleId id);
}