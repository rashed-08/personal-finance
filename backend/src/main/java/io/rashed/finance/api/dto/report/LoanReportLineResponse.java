package io.rashed.finance.api.dto.report;

import io.rashed.finance.common.enums.LoanStatus;
import io.rashed.finance.common.enums.LoanType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LoanReportLineResponse(

        UUID loanId,

        String name,

        LoanType loanType,

        BigDecimal principalAmount,

        BigDecimal paidAmount,

        BigDecimal remainingAmount,

        LoanStatus loanStatus,

        List<LoanPaymentHistoryLineResponse> paymentHistory

) {
}
