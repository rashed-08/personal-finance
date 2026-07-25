package io.rashed.finance.application.salarycycle;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.springframework.stereotype.Service;

import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.salarycycle.SalaryCycleRepository;

/**
 * Implements the salary-cycle lifecycle from docs/business/SalaryWorkflow.md:
 * every salary payment closes the currently open cycle (if any) the day
 * before and opens a new one starting on the payment date.
 */
@Service
public class OpenSalaryCycleForIncomeService {

    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");

    private final SalaryCycleRepository repository;

    public OpenSalaryCycleForIncomeService(SalaryCycleRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public SalaryCycleId execute(LocalDate salaryDate) {

        repository.findOpen().ifPresent(open -> repository.save(open.close(salaryDate.minusDays(1))));

        SalaryCycle opened = repository.save(
                SalaryCycle.open(nameFor(salaryDate), salaryDate, salaryDate, null)
        );

        return opened.getId();
    }

    private String nameFor(LocalDate salaryDate) {

        String name = salaryDate.format(NAME_FORMAT);

        if (!repository.existsByName(name)) {
            return name;
        }

        return name + " (" + salaryDate + ")";
    }
}
