package io.rashed.finance.application.salarycycle;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;

@Service
public class ReopenSalaryCycleService {

    private final SalaryCycleRepository repository;

    public ReopenSalaryCycleService(SalaryCycleRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public SalaryCycle execute(SalaryCycleId id) {

        SalaryCycle cycle = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary cycle not found."));

        repository.findOpen().ifPresent(open -> {
            if (!open.getId().equals(id)) {
                throw new IllegalStateException(
                        "Another salary cycle is already open: " + open.getName());
            }
        });

        return repository.save(cycle.reopen());
    }
}
