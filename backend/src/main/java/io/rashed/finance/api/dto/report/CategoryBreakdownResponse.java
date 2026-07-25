package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryBreakdownResponse(

        UUID categoryId,

        String categoryName,

        BigDecimal total,

        long transactionCount

) {
}
