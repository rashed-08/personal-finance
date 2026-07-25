package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.infrastructure.persistence.entity.LoanEntity;

public final class LoanEntityMapper {

    private LoanEntityMapper() {
    }

    public static LoanEntity toEntity(Loan loan) {

        if (loan == null) {
            return null;
        }

        return new LoanEntity(
                loan.getId().getValue(),
                loan.getName(),
                loan.getLoanType(),
                loan.getPrincipalAmount().getAmount(),
                loan.getStartDate(),
                loan.getDueDate(),
                loan.getLoanStatus(),
                loan.getDescription(),
                loan.getCreatedAt(),
                loan.getUpdatedAt()
        );
    }

    public static Loan toDomain(LoanEntity entity) {

        if (entity == null) {
            return null;
        }

        return new Loan(
                LoanId.of(entity.getId()),
                entity.getName(),
                entity.getLoanType(),
                Money.of(entity.getPrincipalAmount()),
                entity.getStartDate(),
                entity.getDueDate(),
                entity.getLoanStatus(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
