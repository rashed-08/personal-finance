package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.recurring.CreateRecurringTransactionRequest;
import io.rashed.finance.api.dto.recurring.RecurringTransactionDtoMapper;
import io.rashed.finance.api.dto.recurring.RecurringTransactionExecutionResponse;
import io.rashed.finance.api.dto.recurring.RecurringTransactionResponse;
import io.rashed.finance.api.dto.recurring.UpdateRecurringTransactionRequest;
import io.rashed.finance.application.recurring.ActivateRecurringTransactionService;
import io.rashed.finance.application.recurring.CreateRecurringTransactionService;
import io.rashed.finance.application.recurring.DeactivateRecurringTransactionService;
import io.rashed.finance.application.recurring.DeleteRecurringTransactionService;
import io.rashed.finance.application.recurring.GenerateRecurringTransactionNowService;
import io.rashed.finance.application.recurring.GetRecurringTransactionService;
import io.rashed.finance.application.recurring.ListDueRecurringTransactionsService;
import io.rashed.finance.application.recurring.ListRecurringTransactionExecutionsService;
import io.rashed.finance.application.recurring.ListRecurringTransactionsService;
import io.rashed.finance.application.recurring.RunDueRecurringTransactionsService;
import io.rashed.finance.application.recurring.UpdateRecurringTransactionService;
import io.rashed.finance.domain.recurring.RecurringTransaction;
import io.rashed.finance.domain.recurring.RecurringTransactionId;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recurring-transactions")
public class RecurringTransactionController {

    private final CreateRecurringTransactionService createRecurringTransactionService;
    private final ListRecurringTransactionsService listRecurringTransactionsService;
    private final GetRecurringTransactionService getRecurringTransactionService;
    private final UpdateRecurringTransactionService updateRecurringTransactionService;
    private final ActivateRecurringTransactionService activateRecurringTransactionService;
    private final DeactivateRecurringTransactionService deactivateRecurringTransactionService;
    private final DeleteRecurringTransactionService deleteRecurringTransactionService;
    private final ListDueRecurringTransactionsService listDueRecurringTransactionsService;
    private final RunDueRecurringTransactionsService runDueRecurringTransactionsService;
    private final GenerateRecurringTransactionNowService generateRecurringTransactionNowService;
    private final ListRecurringTransactionExecutionsService listRecurringTransactionExecutionsService;

    public RecurringTransactionController(
            CreateRecurringTransactionService createRecurringTransactionService,
            ListRecurringTransactionsService listRecurringTransactionsService,
            GetRecurringTransactionService getRecurringTransactionService,
            UpdateRecurringTransactionService updateRecurringTransactionService,
            ActivateRecurringTransactionService activateRecurringTransactionService,
            DeactivateRecurringTransactionService deactivateRecurringTransactionService,
            DeleteRecurringTransactionService deleteRecurringTransactionService,
            ListDueRecurringTransactionsService listDueRecurringTransactionsService,
            RunDueRecurringTransactionsService runDueRecurringTransactionsService,
            GenerateRecurringTransactionNowService generateRecurringTransactionNowService,
            ListRecurringTransactionExecutionsService listRecurringTransactionExecutionsService
    ) {
        this.createRecurringTransactionService = createRecurringTransactionService;
        this.listRecurringTransactionsService = listRecurringTransactionsService;
        this.getRecurringTransactionService = getRecurringTransactionService;
        this.updateRecurringTransactionService = updateRecurringTransactionService;
        this.activateRecurringTransactionService = activateRecurringTransactionService;
        this.deactivateRecurringTransactionService = deactivateRecurringTransactionService;
        this.deleteRecurringTransactionService = deleteRecurringTransactionService;
        this.listDueRecurringTransactionsService = listDueRecurringTransactionsService;
        this.runDueRecurringTransactionsService = runDueRecurringTransactionsService;
        this.generateRecurringTransactionNowService = generateRecurringTransactionNowService;
        this.listRecurringTransactionExecutionsService = listRecurringTransactionExecutionsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringTransactionResponse create(@Valid @RequestBody CreateRecurringTransactionRequest request) {

        RecurringTransaction recurringTransaction =
                createRecurringTransactionService.create(RecurringTransactionDtoMapper.toCommand(request));

        return RecurringTransactionDtoMapper.toResponse(recurringTransaction);
    }

    @GetMapping
    public List<RecurringTransactionResponse> list(@RequestParam(defaultValue = "false") boolean activeOnly) {

        return listRecurringTransactionsService.execute(activeOnly)
                .stream()
                .map(RecurringTransactionDtoMapper::toResponse)
                .toList();
    }

    @GetMapping("/due")
    public List<RecurringTransactionResponse> listDue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        return listDueRecurringTransactionsService.execute(asOfDate != null ? asOfDate : LocalDate.now())
                .stream()
                .map(RecurringTransactionDtoMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public RecurringTransactionResponse getById(@PathVariable UUID id) {

        return RecurringTransactionDtoMapper.toResponse(
                getRecurringTransactionService.execute(RecurringTransactionId.of(id))
        );
    }

    @GetMapping("/{id}/executions")
    public List<RecurringTransactionExecutionResponse> executions(@PathVariable UUID id) {

        return listRecurringTransactionExecutionsService.execute(RecurringTransactionId.of(id))
                .stream()
                .map(RecurringTransactionDtoMapper::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public RecurringTransactionResponse update(
            @PathVariable UUID id, @Valid @RequestBody UpdateRecurringTransactionRequest request) {

        RecurringTransaction recurringTransaction =
                updateRecurringTransactionService.execute(RecurringTransactionDtoMapper.toCommand(id, request));

        return RecurringTransactionDtoMapper.toResponse(recurringTransaction);
    }

    @PatchMapping("/{id}/activate")
    public RecurringTransactionResponse activate(@PathVariable UUID id) {

        return RecurringTransactionDtoMapper.toResponse(
                activateRecurringTransactionService.execute(RecurringTransactionId.of(id))
        );
    }

    @PatchMapping("/{id}/deactivate")
    public RecurringTransactionResponse deactivate(@PathVariable UUID id) {

        return RecurringTransactionDtoMapper.toResponse(
                deactivateRecurringTransactionService.execute(RecurringTransactionId.of(id))
        );
    }

    @PostMapping("/{id}/generate-now")
    public RecurringTransactionResponse generateNow(@PathVariable UUID id) {

        return RecurringTransactionDtoMapper.toResponse(
                generateRecurringTransactionNowService.execute(RecurringTransactionId.of(id))
        );
    }

    @PostMapping("/run-due")
    public List<RecurringTransactionResponse> runDue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate) {

        return runDueRecurringTransactionsService.execute(asOfDate != null ? asOfDate : LocalDate.now())
                .stream()
                .map(RecurringTransactionDtoMapper::toResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {

        deleteRecurringTransactionService.execute(RecurringTransactionId.of(id));
    }
}
