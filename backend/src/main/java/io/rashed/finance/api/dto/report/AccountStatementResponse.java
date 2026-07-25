package io.rashed.finance.api.dto.report;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AccountStatementResponse(

        UUID accountId,

        String accountName,

        BigDecimal openingBalance,

        List<StatementLineResponse> lines,

        BigDecimal endingBalance

) {
}
