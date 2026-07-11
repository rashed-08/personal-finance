package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.account.AccountDtoMapper;
import io.rashed.finance.api.dto.account.AccountResponse;
import io.rashed.finance.api.dto.account.CreateAccountRequest;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.application.account.CreateAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final CreateAccountService createAccountService;

    public AccountController(
            CreateAccountService createAccountService
    ) {
        this.createAccountService = createAccountService;
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
}