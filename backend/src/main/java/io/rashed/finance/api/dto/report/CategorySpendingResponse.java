package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.util.UUID;

public record CategorySpendingResponse(

        UUID categoryId,

        String categoryName,

        BigDecimal totalSpent

) {
}
