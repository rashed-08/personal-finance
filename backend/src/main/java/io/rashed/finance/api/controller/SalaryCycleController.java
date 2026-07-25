package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.salarycycle.CarryForwardResponse;
import io.rashed.finance.api.dto.salarycycle.CloseSalaryCycleRequest;
import io.rashed.finance.api.dto.salarycycle.CreateSalaryCycleRequest;
import io.rashed.finance.api.dto.salarycycle.SalaryCycleDtoMapper;
import io.rashed.finance.api.dto.salarycycle.SalaryCycleResponse;
import io.rashed.finance.api.dto.salarycycle.UpdateSalaryCycleRequest;
import io.rashed.finance.application.salarycycle.CalculateCarryForwardService;
import io.rashed.finance.application.salarycycle.CloseSalaryCycleService;
import io.rashed.finance.application.salarycycle.CreateSalaryCycleService;
import io.rashed.finance.application.salarycycle.GetCurrentSalaryCycleService;
import io.rashed.finance.application.salarycycle.GetSalaryCycleService;
import io.rashed.finance.application.salarycycle.ListSalaryCyclesService;
import io.rashed.finance.application.salarycycle.ReopenSalaryCycleService;
import io.rashed.finance.application.salarycycle.UpdateSalaryCycleCommand;
import io.rashed.finance.application.salarycycle.UpdateSalaryCycleService;
import io.rashed.finance.domain.salarycycle.SalaryCycle;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/salary-cycles")
public class SalaryCycleController {

    private final CreateSalaryCycleService createSalaryCycleService;
    private final ListSalaryCyclesService listSalaryCyclesService;
    private final GetSalaryCycleService getSalaryCycleService;
    private final GetCurrentSalaryCycleService getCurrentSalaryCycleService;
    private final UpdateSalaryCycleService updateSalaryCycleService;
    private final CloseSalaryCycleService closeSalaryCycleService;
    private final ReopenSalaryCycleService reopenSalaryCycleService;
    private final CalculateCarryForwardService calculateCarryForwardService;

    public SalaryCycleController(
            CreateSalaryCycleService createSalaryCycleService,
            ListSalaryCyclesService listSalaryCyclesService,
            GetSalaryCycleService getSalaryCycleService,
            GetCurrentSalaryCycleService getCurrentSalaryCycleService,
            UpdateSalaryCycleService updateSalaryCycleService,
            CloseSalaryCycleService closeSalaryCycleService,
            ReopenSalaryCycleService reopenSalaryCycleService,
            CalculateCarryForwardService calculateCarryForwardService
    ) {
        this.createSalaryCycleService = createSalaryCycleService;
        this.listSalaryCyclesService = listSalaryCyclesService;
        this.getSalaryCycleService = getSalaryCycleService;
        this.getCurrentSalaryCycleService = getCurrentSalaryCycleService;
        this.updateSalaryCycleService = updateSalaryCycleService;
        this.closeSalaryCycleService = closeSalaryCycleService;
        this.reopenSalaryCycleService = reopenSalaryCycleService;
        this.calculateCarryForwardService = calculateCarryForwardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SalaryCycleResponse create(@Valid @RequestBody CreateSalaryCycleRequest request) {

        SalaryCycle salaryCycle = createSalaryCycleService.create(
                SalaryCycleDtoMapper.toCommand(request)
        );

        return SalaryCycleDtoMapper.toResponse(salaryCycle);
    }

    @GetMapping
    public List<SalaryCycleResponse> list() {

        return SalaryCycleDtoMapper.toResponseList(listSalaryCyclesService.execute());
    }

    @GetMapping("/current")
    public SalaryCycleResponse getCurrent() {

        return SalaryCycleDtoMapper.toResponse(getCurrentSalaryCycleService.execute());
    }

    @GetMapping("/{id}")
    public SalaryCycleResponse getById(@PathVariable UUID id) {

        return SalaryCycleDtoMapper.toResponse(
                getSalaryCycleService.execute(SalaryCycleId.of(id))
        );
    }

    @PutMapping("/{id}")
    public SalaryCycleResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSalaryCycleRequest request
    ) {

        SalaryCycle salaryCycle = updateSalaryCycleService.execute(
                new UpdateSalaryCycleCommand(
                        SalaryCycleId.of(id),
                        request.name(),
                        request.salaryDate(),
                        request.description()
                )
        );

        return SalaryCycleDtoMapper.toResponse(salaryCycle);
    }

    @PatchMapping("/{id}/close")
    public SalaryCycleResponse close(
            @PathVariable UUID id,
            @Valid @RequestBody CloseSalaryCycleRequest request
    ) {

        SalaryCycle salaryCycle = closeSalaryCycleService.execute(
                SalaryCycleId.of(id),
                request.endDate()
        );

        return SalaryCycleDtoMapper.toResponse(salaryCycle);
    }

    @PatchMapping("/{id}/reopen")
    public SalaryCycleResponse reopen(@PathVariable UUID id) {

        SalaryCycle salaryCycle = reopenSalaryCycleService.execute(SalaryCycleId.of(id));

        return SalaryCycleDtoMapper.toResponse(salaryCycle);
    }

    @GetMapping("/{id}/carry-forward")
    public CarryForwardResponse carryForward(@PathVariable UUID id) {

        return SalaryCycleDtoMapper.toResponse(
                calculateCarryForwardService.execute(SalaryCycleId.of(id))
        );
    }
}
