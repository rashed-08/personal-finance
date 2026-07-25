package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.salarycycle.CreateSalaryCycleRequest;
import io.rashed.finance.api.dto.salarycycle.SalaryCycleDtoMapper;
import io.rashed.finance.api.dto.salarycycle.SalaryCycleResponse;
import io.rashed.finance.application.salarycycle.CreateSalaryCycleService;
import io.rashed.finance.application.salarycycle.GetSalaryCycleService;
import io.rashed.finance.application.salarycycle.ListSalaryCyclesService;
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

    public SalaryCycleController(
            CreateSalaryCycleService createSalaryCycleService,
            ListSalaryCyclesService listSalaryCyclesService,
            GetSalaryCycleService getSalaryCycleService
    ) {
        this.createSalaryCycleService = createSalaryCycleService;
        this.listSalaryCyclesService = listSalaryCyclesService;
        this.getSalaryCycleService = getSalaryCycleService;
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

    @GetMapping("/{id}")
    public SalaryCycleResponse getById(@PathVariable UUID id) {

        return SalaryCycleDtoMapper.toResponse(
                getSalaryCycleService.execute(SalaryCycleId.of(id))
        );
    }
}
