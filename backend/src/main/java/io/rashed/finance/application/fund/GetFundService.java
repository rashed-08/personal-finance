package io.rashed.finance.application.fund;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.funds.FundRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class GetFundService {

    private final FundRepository repository;

    public GetFundService(FundRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Fund execute(FundId id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found: " + id.getValue()));
    }
}
