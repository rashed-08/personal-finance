package io.rashed.finance.application.salarycycle;

import java.util.Objects;

import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;

public class CreateSalaryCycleService {

    private final SalaryCycleRepository repository;

    public CreateSalaryCycleService(SalaryCycleRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public SalaryCycle create(CreateSalaryCycleCommand command) {

        Objects.requireNonNull(command);

        if (repository.existsByName(command.name())) {
            throw new IllegalArgumentException(
                    "Salary cycle already exists: " + command.name()
            );
        }

        SalaryCycle salaryCycle = SalaryCycle.create(
                command.name(),
                command.period(),
                command.salaryDate(),
                command.description()
        );

        return repository.save(salaryCycle);
    }
}