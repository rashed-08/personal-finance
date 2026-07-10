package io.rashed.finance.domain.funds;

import java.util.Objects;

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