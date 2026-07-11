package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.account.AccountDtoMapper;
import io.rashed.finance.api.dto.account.AccountResponse;
import io.rashed.finance.api.dto.account.CreateAccountRequest;
import io.rashed.finance.api.dto.account.UpdateAccountRequest;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.application.account.CreateAccountService;
import io.rashed.finance.application.account.DeactivateAccountService;
import io.rashed.finance.application.account.DeleteAccountService;
import io.rashed.finance.application.account.ListAccountsService;
import io.rashed.finance.application.account.UpdateAccountService;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.application.account.GetAccountService;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final CreateAccountService createAccountService;
    private final ListAccountsService listAccountsService;
    private final GetAccountService getAccountService;
    private final UpdateAccountService updateAccountService;
    private final DeactivateAccountService deactivateAccountService;
    private final DeleteAccountService deleteAccountService;

    public AccountController(
            CreateAccountService createAccountService,
            ListAccountsService listAccountsService,
            GetAccountService getAccountService,
            UpdateAccountService updateAccountService,
            DeactivateAccountService deactivateAccountService,
            DeleteAccountService deleteAccountService
    ) {
        this.createAccountService = createAccountService;
        this.listAccountsService = listAccountsService;
        this.getAccountService = getAccountService;
        this.updateAccountService = updateAccountService;
        this.deactivateAccountService = deactivateAccountService;
        this.deleteAccountService = deleteAccountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(
            @Valid @RequestBody CreateAccountRequest request
    ) {

        Account account = createAccountService.execute(
                AccountDtoMapper.toCommand(request)
        );

        return AccountDtoMapper.toResponse(account);
    }

    @GetMapping
    public List<AccountResponse> getAccounts() {

        return AccountDtoMapper.toResponseList(listAccountsService.execute());
    }

    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable UUID id) {

        Account account = getAccountService.findById(AccountId.of(id));

        return AccountDtoMapper.toResponse(account);
    }

    @PutMapping("/{id}")
    public AccountResponse updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request
    ){

        Account account = updateAccountService.execute(
            AccountId.of(id),
            request.name(),
            request.accountType(),
            Money.of(request.openingBalance()),
            request.description()
        );

        return AccountDtoMapper.toResponse(account);

    }

    @PatchMapping("/{id}/deactivate")
    public AccountResponse deactivate(
            @PathVariable UUID id
    ){

        Account account =
                deactivateAccountService.execute(
                        AccountId.of(id)
                );


        return AccountDtoMapper.toResponse(account);

    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable UUID id
    ){

        deleteAccountService.execute(
                AccountId.of(id)
        );

    }
}