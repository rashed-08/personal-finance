package io.rashed.finance.application.salarycycle;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;

@Service
public class UpdateSalaryCycleService {

    private final SalaryCycleRepository repository;

    public UpdateSalaryCycleService(SalaryCycleRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public SalaryCycle execute(UpdateSalaryCycleCommand command) {

        SalaryCycle cycle = repository.findById(command.id())
                .orElseThrow(() -> new ResourceNotFoundException("Salary cycle not found."));

        SalaryCycle updated = cycle.update(command.name(), command.salaryDate(), command.description());

        return repository.save(updated);
    }
}
