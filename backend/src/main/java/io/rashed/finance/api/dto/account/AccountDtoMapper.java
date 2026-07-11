package io.rashed.finance.api.dto.account;

import io.rashed.finance.application.account.CreateAccountCommand;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;

public final class AccountDtoMapper {

    private AccountDtoMapper() {
    }

    public static CreateAccountCommand toCommand(
            CreateAccountRequest request
    ) {

        if (request == null) {
            return null;
        }

        return new CreateAccountCommand(
                request.name(),
                request.accountType(),
                Money.of(request.openingBalance()),
                request.description()
        );
    }

    public static AccountResponse toResponse(
            Account account
    ) {

        if (account == null) {
            return null;
        }

        return new AccountResponse(
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
}