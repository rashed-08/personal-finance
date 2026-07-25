package io.rashed.finance.application.report;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.rashed.finance.application.fund.CalculateFundBalanceService;
import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.domain.funds.FundRepository;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

/**
 * Target/Allocated/Used/Remaining/Progress per
 * docs/requirements/FunctionalRequirements.md FR-011 and
 * docs/database/tables/transactions/07-reporting.md 5.4.
 */
@Service
public class GetFundReportService {

    private final FundRepository fundRepository;
    private final TransactionRepository transactionRepository;
    private final CalculateFundBalanceService calculateFundBalanceService;

    public GetFundReportService(
            FundRepository fundRepository,
            TransactionRepository transactionRepository,
            CalculateFundBalanceService calculateFundBalanceService
    ) {
        this.fundRepository = Objects.requireNonNull(fundRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.calculateFundBalanceService = Objects.requireNonNull(calculateFundBalanceService);
    }

    public List<FundReportLine> execute(boolean activeOnly) {

        List<Fund> funds = activeOnly ? fundRepository.findActive() : fundRepository.findAll();

        return funds.stream().map(this::toLine).toList();
    }

    public FundReportLine executeOne(FundId fundId) {

        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new ResourceNotFoundException("Fund not found."));

        return toLine(fund);
    }

    private FundReportLine toLine(Fund fund) {

        TransactionFilter filter = new TransactionFilter(
                null, null, TransactionType.TRANSFER, TransactionStatus.POSTED, null, null, null, fund.getId(), null);

        Money allocated = Money.zero();
        Money used = Money.zero();

        for (Transaction transaction : transactionRepository.find(filter, Pageable.unpaged())) {

            if (transaction.increasesFundBalance()) {
                allocated = allocated.add(transaction.getAmount());
            } else {
                used = used.add(transaction.getAmount());
            }
        }

        Money remaining = calculateFundBalanceService.execute(fund.getId());

        BigDecimal progress = fund.hasTargetAmount() && fund.getTargetAmount().isPositive()
                ? remaining.getAmount()
                        .divide(fund.getTargetAmount().getAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : null;

        return new FundReportLine(
                fund.getId(),
                fund.getName(),
                fund.getFundType(),
                fund.hasTargetAmount() ? fund.getTargetAmount() : null,
                allocated,
                used,
                remaining,
                progress
        );
    }
}
