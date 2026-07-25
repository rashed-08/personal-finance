package io.rashed.finance.api.dto.salarycycle;

import java.math.BigDecimal;
import java.util.UUID;

public record CarryForwardResponse(

        UUID salaryCycleId,

        BigDecimal openingBalance,

        BigDecimal income,

        BigDecimal expenses,

        BigDecimal adjustments,

        BigDecimal closingBalance
) {
}
