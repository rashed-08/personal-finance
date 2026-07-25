package io.rashed.finance.application.salarycycle;

import java.util.List;

import org.springframework.stereotype.Service;

import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;

@Service
public class ListSalaryCyclesService {

    private final SalaryCycleRepository repository;

    public ListSalaryCyclesService(SalaryCycleRepository repository) {
        this.repository = repository;
    }

    public List<SalaryCycle> execute() {
        return repository.findAll();
    }
}
