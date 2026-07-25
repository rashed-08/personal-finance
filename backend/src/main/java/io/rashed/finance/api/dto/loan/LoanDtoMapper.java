package io.rashed.finance.api.dto.loan;

import io.rashed.finance.application.loan.CreateLoanCommand;
import io.rashed.finance.application.loan.RecordRepaymentCommand;
import io.rashed.finance.application.loan.UpdateLoanCommand;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;

import java.util.UUID;

public final class LoanDtoMapper {

    private LoanDtoMapper() {
    }

    public static CreateLoanCommand toCommand(CreateLoanRequest request) {

        return new CreateLoanCommand(
                request.name(),
                request.loanType(),
                Money.of(request.principalAmount()),
                request.startDate(),
                request.dueDate(),
                AccountId.of(request.accountId()),
                SalaryCycleId.of(request.salaryCycleId()),
                request.description()
        );
    }

    public static UpdateLoanCommand toCommand(UUID id, UpdateLoanRequest request) {

        return new UpdateLoanCommand(
                LoanId.of(id),
                request.name(),
                request.dueDate(),
                request.description()
        );
    }

    public static RecordRepaymentCommand toCommand(UUID loanId, RecordRepaymentRequest request) {

        return new RecordRepaymentCommand(
                LoanId.of(loanId),
                AccountId.of(request.accountId()),
                Money.of(request.amount()),
                request.paymentDate(),
                SalaryCycleId.of(request.salaryCycleId()),
                request.description()
        );
    }

    public static LoanResponse toResponse(Loan loan, Money outstandingBalance) {

        return new LoanResponse(
                loan.getId().getValue(),
                loan.getName(),
                loan.getLoanType(),
                loan.getPrincipalAmount().getAmount(),
                loan.getStartDate(),
                loan.getDueDate(),
                outstandingBalance.getAmount(),
                loan.getLoanStatus(),
                loan.getDescription(),
                loan.getCreatedAt(),
                loan.getUpdatedAt()
        );
    }
}
