package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.transaction.CreateTransactionRequest;
import io.rashed.finance.api.dto.transaction.TransactionDtoMapper;
import io.rashed.finance.api.dto.transaction.TransactionFilterMapper;
import io.rashed.finance.api.dto.transaction.TransactionFilterRequest;
import io.rashed.finance.api.dto.transaction.TransactionResponse;
import io.rashed.finance.api.dto.transaction.UpdateTransactionRequest;
import io.rashed.finance.application.transaction.CreateTransactionCommand;
import io.rashed.finance.application.transaction.CreateTransactionService;
import io.rashed.finance.application.transaction.ReverseTransactionService;
import io.rashed.finance.application.transaction.UpdateTransactionService;
import io.rashed.finance.application.transaction.VoidTransactionService;
import io.rashed.finance.application.transaction.query.GetTransactionService;
import io.rashed.finance.application.transaction.query.ListTransactionsService;
import io.rashed.finance.application.transaction.validation.CreateTransactionRequestValidator;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CreateTransactionService createTransactionService;
    private final GetTransactionService getTransactionService;
    private final ListTransactionsService listTransactionsService;
    private final VoidTransactionService voidTransactionService;
    private final ReverseTransactionService reverseTransactionService;
    private final UpdateTransactionService updateTransactionService;

    public TransactionController(CreateTransactionService createTransactionService, GetTransactionService getTransactionService, ListTransactionsService listTransactionsService, VoidTransactionService voidTransactionService, ReverseTransactionService reverseTransactionService, UpdateTransactionService updateTransactionService) {
        this.createTransactionService = createTransactionService;
        this.getTransactionService = getTransactionService;
        this.listTransactionsService = listTransactionsService;
        this.voidTransactionService = voidTransactionService;
        this.reverseTransactionService = reverseTransactionService;
        this.updateTransactionService = updateTransactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse create(
            @Valid @RequestBody CreateTransactionRequest request) {

        CreateTransactionRequestValidator.validate(request);

        CreateTransactionCommand command =
                TransactionDtoMapper.toCommand(request);

        Transaction transaction =
                createTransactionService.execute(command);

        return TransactionDtoMapper.toResponse(transaction);
    }

    @GetMapping
    public Page<TransactionResponse> list(TransactionFilterRequest request,@PageableDefault(
                    size = 20,
                    sort = "transactionDate",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {


        return listTransactionsService
                .execute(
                        TransactionFilterMapper.toDomain(request),
                        pageable
                )
                .map(TransactionDtoMapper::toResponse);

    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable UUID id) {

        return TransactionDtoMapper.toResponse(
                getTransactionService.execute(TransactionId.of(id))
        );
    }

    @PatchMapping("/{id}/void")
    public TransactionResponse voidTransaction(
            @PathVariable UUID id) {

        Transaction transaction =
                voidTransactionService.execute(TransactionId.of(id));

        return TransactionDtoMapper.toResponse(transaction);
    }

    @PatchMapping("/{id}/reverse")
    public TransactionResponse reverseTransaction(
            @PathVariable UUID id) {

        Transaction transaction =
                reverseTransactionService.execute(TransactionId.of(id));

        return TransactionDtoMapper.toResponse(transaction);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable UUID id,
            @Valid
            @RequestBody UpdateTransactionRequest request) {


        Transaction transaction =
                updateTransactionService.execute(

                        TransactionDtoMapper.toCommand(
                                id,
                                request
                        )

                );


        return TransactionDtoMapper.toResponse(
                transaction
        );

    }
}