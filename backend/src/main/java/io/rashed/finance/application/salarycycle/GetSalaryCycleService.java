package io.rashed.finance.application.salarycycle;

import org.springframework.stereotype.Service;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;

@Service
public class GetSalaryCycleService {

    private final SalaryCycleRepository repository;

    public GetSalaryCycleService(SalaryCycleRepository repository) {
        this.repository = repository;
    }

    public SalaryCycle execute(SalaryCycleId id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary cycle not found."));
    }
}
