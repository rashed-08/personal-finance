package io.rashed.finance.application.fund;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.funds.FundRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ActivateFundService {

    private final FundRepository repository;

    public ActivateFundService(FundRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Fund execute(FundId id) {

        Fund fund = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found: " + id.getValue()));

        return repository.save(fund.activate());
    }
}
