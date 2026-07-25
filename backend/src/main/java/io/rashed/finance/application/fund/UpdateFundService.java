package io.rashed.finance.application.fund;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UpdateFundService {

    private final FundRepository repository;

    public UpdateFundService(FundRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Fund execute(UpdateFundCommand command) {

        Objects.requireNonNull(command, "Command cannot be null.");

        Fund fund = repository.findById(command.fundId())
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found: " + command.fundId().getValue()));

        Fund updated = fund
                .rename(command.name())
                .changeTarget(command.targetAmount(), command.targetDate())
                .changeDescription(command.description());

        return repository.save(updated);
    }
}
