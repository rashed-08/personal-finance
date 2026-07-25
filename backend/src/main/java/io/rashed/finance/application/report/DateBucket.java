package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;

import java.time.LocalDate;

public record DateBucket(

        LocalDate date,

        Money total

) {
}
