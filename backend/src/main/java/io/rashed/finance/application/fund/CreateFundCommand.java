package io.rashed.finance.application.fund;

import io.rashed.finance.common.enums.FundType;
import io.rashed.finance.common.valueobject.Money;

import java.time.LocalDate;

public record CreateFundCommand(

        String name,

        FundType fundType,

        Money targetAmount,

        LocalDate targetDate,

        String description

) {
}
