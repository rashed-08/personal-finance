package io.rashed.finance.api.dto.loan;

import io.rashed.finance.common.enums.LoanType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateLoanRequest(

        @NotBlank(message = "Loan name is required.")
        @Size(max = 150, message = "Loan name cannot exceed 150 characters.")
        String name,

        @NotNull(message = "Loan type is required.")
        LoanType loanType,

        @NotNull(message = "Principal amount is required.")
        @DecimalMin(value = "0.00", inclusive = false, message = "Principal amount must be greater than zero.")
        BigDecimal principalAmount,

        @NotNull(message = "Start date is required.")
        LocalDate startDate,

        LocalDate dueDate,

        @NotNull(message = "Account is required.")
        UUID accountId,

        @NotNull(message = "Salary cycle is required.")
        UUID salaryCycleId,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters.")
        String description

) {
}
