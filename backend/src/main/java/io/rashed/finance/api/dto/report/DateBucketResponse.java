package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DateBucketResponse(

        LocalDate date,

        BigDecimal total

) {
}
