package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.reconciliation.CashReconciliationDtoMapper;
import io.rashed.finance.api.dto.reconciliation.CashReconciliationResponse;
import io.rashed.finance.api.dto.reconciliation.RecordCashSnapshotRequest;
import io.rashed.finance.api.dto.reconciliation.StartReconciliationRequest;
import io.rashed.finance.application.reconciliation.CompleteReconciliationService;
import io.rashed.finance.application.reconciliation.GetReconciliationService;
import io.rashed.finance.application.reconciliation.ListReconciliationsService;
import io.rashed.finance.application.reconciliation.RecordCashSnapshotService;
import io.rashed.finance.application.reconciliation.StartReconciliationService;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationId;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cash-reconciliations")
public class ReconciliationController {

    private final StartReconciliationService startReconciliationService;
    private final RecordCashSnapshotService recordCashSnapshotService;
    private final CompleteReconciliationService completeReconciliationService;
    private final GetReconciliationService getReconciliationService;
    private final ListReconciliationsService listReconciliationsService;

    public ReconciliationController(
            StartReconciliationService startReconciliationService,
            RecordCashSnapshotService recordCashSnapshotService,
            CompleteReconciliationService completeReconciliationService,
            GetReconciliationService getReconciliationService,
            ListReconciliationsService listReconciliationsService
    ) {
        this.startReconciliationService = startReconciliationService;
        this.recordCashSnapshotService = recordCashSnapshotService;
        this.completeReconciliationService = completeReconciliationService;
        this.getReconciliationService = getReconciliationService;
        this.listReconciliationsService = listReconciliationsService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CashReconciliationResponse start(@Valid @RequestBody StartReconciliationRequest request) {

        CashReconciliation reconciliation = startReconciliationService.execute(
                CashReconciliationDtoMapper.toCommand(request)
        );

        return CashReconciliationDtoMapper.toResponse(reconciliation);
    }

    @GetMapping
    public List<CashReconciliationResponse> list(@RequestParam(required = false) UUID accountId) {

        Optional<AccountId> filter = accountId == null ? Optional.empty() : Optional.of(AccountId.of(accountId));

        return CashReconciliationDtoMapper.toResponseList(listReconciliationsService.execute(filter));
    }

    @GetMapping("/{id}")
    public CashReconciliationResponse getById(@PathVariable UUID id) {

        return CashReconciliationDtoMapper.toResponse(
                getReconciliationService.execute(CashReconciliationId.of(id))
        );
    }

    @PostMapping("/{id}/snapshots")
    @ResponseStatus(HttpStatus.CREATED)
    public CashReconciliationResponse recordSnapshot(
            @PathVariable UUID id,
            @Valid @RequestBody RecordCashSnapshotRequest request
    ) {

        CashReconciliation reconciliation = recordCashSnapshotService.execute(
                CashReconciliationId.of(id),
                Money.of(request.cashAmount()),
                request.notes()
        );

        return CashReconciliationDtoMapper.toResponse(reconciliation);
    }

    @PatchMapping("/{id}/complete")
    public CashReconciliationResponse complete(@PathVariable UUID id) {

        CashReconciliation reconciliation = completeReconciliationService.execute(CashReconciliationId.of(id));

        return CashReconciliationDtoMapper.toResponse(reconciliation);
    }
}
