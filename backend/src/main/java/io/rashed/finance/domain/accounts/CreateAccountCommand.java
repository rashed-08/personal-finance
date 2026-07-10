package io.rashed.finance.domain.accounts;

import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.valueobject.Money;

public record CreateAccountCommand(String name, AccountType accountType, Money openingBalance, String description) {
    
}