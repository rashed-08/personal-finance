package io.rashed.finance.application.account;


import io.rashed.finance.domain.accounts.*;

import org.springframework.stereotype.Service;


@Service
public class DeleteAccountService {


    private final AccountRepository repository;


    public DeleteAccountService(
            AccountRepository repository
    ){
        this.repository = repository;
    }



    public void execute(AccountId id){

        repository.deleteById(id);

    }

}