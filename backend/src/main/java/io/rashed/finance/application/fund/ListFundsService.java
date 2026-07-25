package io.rashed.finance.application.fund;

import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ListFundsService {

    private final FundRepository repository;

    public ListFundsService(FundRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public List<Fund> execute(boolean activeOnly) {

        return activeOnly ? repository.findActive() : repository.findAll();
    }
}
