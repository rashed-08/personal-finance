package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.infrastructure.persistence.entity.AccountEntity;

public final class AccountEntityMapper {

    private AccountEntityMapper() {
    }

    public static AccountEntity toEntity(Account account) {

        if (account == null) {
            return null;
        }

        return new AccountEntity(
                account.getId().getValue(),
                account.getName(),
                account.getAccountType(),
                account.getOpeningBalance().getAmount(),
                account.isActive(),
                account.getDescription(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    public static Account toDomain(AccountEntity entity) {

        if (entity == null) {
            return null;
        }

        return new Account(
                AccountId.of(entity.getId()),
                entity.getName(),
                entity.getAccountType(),
                Money.of(entity.getOpeningBalance()),
                entity.isActive(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}