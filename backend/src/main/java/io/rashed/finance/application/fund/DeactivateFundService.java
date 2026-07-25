package io.rashed.finance.application.fund;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.funds.FundRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * A fund may only be closed (deactivated) once its derived balance is zero.
 * See docs/business/FundWorkflow.md.
 */
@Service
public class DeactivateFundService {

    private final FundRepository repository;
    private final CalculateFundBalanceService calculateFundBalanceService;

    public DeactivateFundService(FundRepository repository, CalculateFundBalanceService calculateFundBalanceService) {
        this.repository = Objects.requireNonNull(repository);
        this.calculateFundBalanceService = Objects.requireNonNull(calculateFundBalanceService);
    }

    public Fund execute(FundId id) {

        Fund fund = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found: " + id.getValue()));

        if (!calculateFundBalanceService.execute(id).isZero()) {
            throw new IllegalStateException("Fund cannot be closed while it has a non-zero balance.");
        }

        return repository.save(fund.deactivate());
    }
}
