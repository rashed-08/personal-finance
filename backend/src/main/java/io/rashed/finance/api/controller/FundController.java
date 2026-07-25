package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.fund.CreateFundRequest;
import io.rashed.finance.api.dto.fund.FundDtoMapper;
import io.rashed.finance.api.dto.fund.FundResponse;
import io.rashed.finance.api.dto.fund.UpdateFundRequest;
import io.rashed.finance.application.fund.ActivateFundService;
import io.rashed.finance.application.fund.CalculateFundBalanceService;
import io.rashed.finance.application.fund.CreateFundService;
import io.rashed.finance.application.fund.DeactivateFundService;
import io.rashed.finance.application.fund.GetFundService;
import io.rashed.finance.application.fund.ListFundsService;
import io.rashed.finance.application.fund.UpdateFundService;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.funds.Fund;
import io.rashed.finance.domain.funds.FundId;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/funds")
public class FundController {

    private final CreateFundService createFundService;
    private final ListFundsService listFundsService;
    private final GetFundService getFundService;
    private final UpdateFundService updateFundService;
    private final ActivateFundService activateFundService;
    private final DeactivateFundService deactivateFundService;
    private final CalculateFundBalanceService calculateFundBalanceService;

    public FundController(
            CreateFundService createFundService,
            ListFundsService listFundsService,
            GetFundService getFundService,
            UpdateFundService updateFundService,
            ActivateFundService activateFundService,
            DeactivateFundService deactivateFundService,
            CalculateFundBalanceService calculateFundBalanceService
    ) {
        this.createFundService = createFundService;
        this.listFundsService = listFundsService;
        this.getFundService = getFundService;
        this.updateFundService = updateFundService;
        this.activateFundService = activateFundService;
        this.deactivateFundService = deactivateFundService;
        this.calculateFundBalanceService = calculateFundBalanceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FundResponse create(@Valid @RequestBody CreateFundRequest request) {

        Fund fund = createFundService.create(FundDtoMapper.toCommand(request));

        return toResponseWithBalance(fund);
    }

    @GetMapping
    public List<FundResponse> list(@RequestParam(defaultValue = "false") boolean activeOnly) {

        return listFundsService.execute(activeOnly)
                .stream()
                .map(this::toResponseWithBalance)
                .toList();
    }

    @GetMapping("/{id}")
    public FundResponse getById(@PathVariable UUID id) {

        return toResponseWithBalance(getFundService.execute(FundId.of(id)));
    }

    @PutMapping("/{id}")
    public FundResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateFundRequest request) {

        Fund fund = updateFundService.execute(FundDtoMapper.toCommand(id, request));

        return toResponseWithBalance(fund);
    }

    @PatchMapping("/{id}/activate")
    public FundResponse activate(@PathVariable UUID id) {

        return toResponseWithBalance(activateFundService.execute(FundId.of(id)));
    }

    @PatchMapping("/{id}/deactivate")
    public FundResponse deactivate(@PathVariable UUID id) {

        return toResponseWithBalance(deactivateFundService.execute(FundId.of(id)));
    }

    private FundResponse toResponseWithBalance(Fund fund) {

        Money balance = calculateFundBalanceService.execute(fund.getId());

        return FundDtoMapper.toResponse(fund, balance);
    }
}
