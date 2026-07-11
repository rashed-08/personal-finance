package io.rashed.finance.application.fund;

import java.util.Objects;

import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundRepository;

public class CreateFundService {

    private final FundRepository repository;

    public CreateFundService(FundRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Fund create(CreateFundCommand command) {

        Objects.requireNonNull(command);

        if (repository.existsByName(command.name())) {
            throw new IllegalArgumentException(
                    "Fund already exists: " + command.name()
            );
        }

        Fund fund = Fund.create(
                command.name(),
                command.fundType(),
                command.targetAmount(),
                command.openingBalance(),
                command.description()
        );

        return repository.save(fund);
    }
}