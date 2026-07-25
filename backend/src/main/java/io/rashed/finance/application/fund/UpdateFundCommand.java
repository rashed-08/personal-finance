package io.rashed.finance.application.fund;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.funds.FundId;

import java.time.LocalDate;

public record UpdateFundCommand(

        FundId fundId,

        String name,

        Money targetAmount,

        LocalDate targetDate,

        String description

) {
}
