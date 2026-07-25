package io.rashed.finance.application.salarycycle;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;

@Service
public class CloseSalaryCycleService {

    private final SalaryCycleRepository repository;

    public CloseSalaryCycleService(SalaryCycleRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public SalaryCycle execute(SalaryCycleId id, LocalDate endDate) {

        SalaryCycle cycle = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary cycle not found."));

        return repository.save(cycle.close(endDate));
    }
}
