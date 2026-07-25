package io.rashed.finance.application.report;

import io.rashed.finance.common.enums.LoanStatus;
import io.rashed.finance.common.enums.LoanType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.loans.LoanId;

import java.util.List;

public record LoanReportLine(

        LoanId loanId,

        String name,

        LoanType loanType,

        Money principalAmount,

        Money paidAmount,

        Money remainingAmount,

        LoanStatus loanStatus,

        List<LoanPaymentHistoryLine> paymentHistory

) {
}
