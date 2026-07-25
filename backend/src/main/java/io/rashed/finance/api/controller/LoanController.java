package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.loan.CreateLoanRequest;
import io.rashed.finance.api.dto.loan.LoanDtoMapper;
import io.rashed.finance.api.dto.loan.LoanResponse;
import io.rashed.finance.api.dto.loan.RecordRepaymentRequest;
import io.rashed.finance.api.dto.loan.UpdateLoanRequest;
import io.rashed.finance.application.loan.CalculateLoanBalanceService;
import io.rashed.finance.application.loan.CloseLoanService;
import io.rashed.finance.application.loan.CreateLoanService;
import io.rashed.finance.application.loan.GetLoanService;
import io.rashed.finance.application.loan.ListLoansService;
import io.rashed.finance.application.loan.RecordRepaymentService;
import io.rashed.finance.application.loan.UpdateLoanService;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.loans.Loan;
import io.rashed.finance.domain.loans.LoanId;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final CreateLoanService createLoanService;
    private final ListLoansService listLoansService;
    private final GetLoanService getLoanService;
    private final UpdateLoanService updateLoanService;
    private final RecordRepaymentService recordRepaymentService;
    private final CloseLoanService closeLoanService;
    private final CalculateLoanBalanceService calculateLoanBalanceService;

    public LoanController(
            CreateLoanService createLoanService,
            ListLoansService listLoansService,
            GetLoanService getLoanService,
            UpdateLoanService updateLoanService,
            RecordRepaymentService recordRepaymentService,
            CloseLoanService closeLoanService,
            CalculateLoanBalanceService calculateLoanBalanceService
    ) {
        this.createLoanService = createLoanService;
        this.listLoansService = listLoansService;
        this.getLoanService = getLoanService;
        this.updateLoanService = updateLoanService;
        this.recordRepaymentService = recordRepaymentService;
        this.closeLoanService = closeLoanService;
        this.calculateLoanBalanceService = calculateLoanBalanceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanResponse create(@Valid @RequestBody CreateLoanRequest request) {

        Loan loan = createLoanService.create(LoanDtoMapper.toCommand(request));

        return toResponseWithBalance(loan);
    }

    @GetMapping
    public List<LoanResponse> list(@RequestParam(defaultValue = "false") boolean activeOnly) {

        return listLoansService.execute(activeOnly)
                .stream()
                .map(this::toResponseWithBalance)
                .toList();
    }

    @GetMapping("/{id}")
    public LoanResponse getById(@PathVariable UUID id) {

        return toResponseWithBalance(getLoanService.execute(LoanId.of(id)));
    }

    @PutMapping("/{id}")
    public LoanResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateLoanRequest request) {

        Loan loan = updateLoanService.execute(LoanDtoMapper.toCommand(id, request));

        return toResponseWithBalance(loan);
    }

    @PatchMapping("/{id}/repay")
    public LoanResponse repay(@PathVariable UUID id, @Valid @RequestBody RecordRepaymentRequest request) {

        Loan loan = recordRepaymentService.execute(LoanDtoMapper.toCommand(id, request));

        return toResponseWithBalance(loan);
    }

    @PatchMapping("/{id}/close")
    public LoanResponse close(@PathVariable UUID id) {

        return toResponseWithBalance(closeLoanService.execute(LoanId.of(id)));
    }

    private LoanResponse toResponseWithBalance(Loan loan) {

        Money outstandingBalance = calculateLoanBalanceService.execute(loan.getId());

        return LoanDtoMapper.toResponse(loan, outstandingBalance);
    }
}
