package io.rashed.finance.application.salarycycle;

import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;

@Service
public class GetCurrentSalaryCycleService {

    private final SalaryCycleRepository repository;

    public GetCurrentSalaryCycleService(SalaryCycleRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public SalaryCycle execute() {

        return repository.findCurrent()
                .orElseThrow(() -> new ResourceNotFoundException("No current salary cycle."));
    }
}
