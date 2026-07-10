package io.rashed.finance.domain.funds;

import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.common.valueobject.Money;

public record CreateFundCommand(

        String name,

        FundType fundType,

        Money targetAmount,

        Money openingBalance,

        String description

) {
}