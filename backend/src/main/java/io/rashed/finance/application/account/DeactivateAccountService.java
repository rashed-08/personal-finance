package io.rashed.finance.application.account;


import io.rashed.finance.domain.accounts.*;

import org.springframework.stereotype.Service;


@Service
public class DeactivateAccountService {


    private final AccountRepository repository;


    public DeactivateAccountService(
            AccountRepository repository
    ){
        this.repository = repository;
    }



    public Account execute(AccountId id){

        Account account =
                repository.findById(id)
                        .orElseThrow();


        account.deactivate();


        return repository.save(account);
    }

}