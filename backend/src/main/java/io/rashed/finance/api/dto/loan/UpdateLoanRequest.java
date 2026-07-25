package io.rashed.finance.api.dto.loan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateLoanRequest(

        @NotBlank(message = "Loan name is required.")
        @Size(max = 150)
        String name,

        LocalDate dueDate,

        @Size(max = 1000)
        String description

) {
}
