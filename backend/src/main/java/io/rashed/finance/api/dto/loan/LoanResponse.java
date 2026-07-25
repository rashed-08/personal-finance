package io.rashed.finance.api.dto.loan;

import io.rashed.finance.common.enums.LoanStatus;
import io.rashed.finance.common.enums.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LoanResponse(

        UUID id,

        String name,

        LoanType loanType,

        BigDecimal principalAmount,

        LocalDate startDate,

        LocalDate dueDate,

        BigDecimal outstandingBalance,

        LoanStatus loanStatus,

        String description,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}
