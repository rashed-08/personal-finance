package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import io.rashed.finance.infrastructure.persistence.entity.FundEntity;

public final class FundEntityMapper {

    private FundEntityMapper() {
    }

    public static FundEntity toEntity(Fund fund) {

        if (fund == null) {
            return null;
        }

        return new FundEntity(
                fund.getId().getValue(),
                fund.getName(),
                fund.getFundType(),
                fund.hasTargetAmount() ? fund.getTargetAmount().getAmount() : null,
                fund.getTargetDate(),
                fund.isActive(),
                fund.getDescription(),
                fund.getCreatedAt(),
                fund.getUpdatedAt()
        );
    }

    public static Fund toDomain(FundEntity entity) {

        if (entity == null) {
            return null;
        }

        return new Fund(
                FundId.of(entity.getId()),
                entity.getName(),
                entity.getFundType(),
                entity.getTargetAmount() != null ? Money.of(entity.getTargetAmount()) : null,
                entity.getTargetDate(),
                entity.isActive(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
