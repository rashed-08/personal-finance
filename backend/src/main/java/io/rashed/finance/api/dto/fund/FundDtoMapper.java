package io.rashed.finance.api.dto.fund;

import io.rashed.finance.application.fund.CreateFundCommand;
import io.rashed.finance.application.fund.UpdateFundCommand;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;

import java.util.UUID;

public final class FundDtoMapper {

    private FundDtoMapper() {
    }

    public static CreateFundCommand toCommand(CreateFundRequest request) {

        return new CreateFundCommand(
                request.name(),
                request.fundType(),
                request.targetAmount() == null ? null : Money.of(request.targetAmount()),
                request.targetDate(),
                request.description()
        );
    }

    public static UpdateFundCommand toCommand(UUID id, UpdateFundRequest request) {

        return new UpdateFundCommand(
                FundId.of(id),
                request.name(),
                request.targetAmount() == null ? null : Money.of(request.targetAmount()),
                request.targetDate(),
                request.description()
        );
    }

    public static FundResponse toResponse(Fund fund, Money balance) {

        return new FundResponse(
                fund.getId().getValue(),
                fund.getName(),
                fund.getFundType(),
                fund.hasTargetAmount() ? fund.getTargetAmount().getAmount() : null,
                fund.getTargetDate(),
                balance.getAmount(),
                fund.isActive(),
                fund.getDescription(),
                fund.getCreatedAt(),
                fund.getUpdatedAt()
        );
    }
}
