package io.rashed.finance.application.transaction;


import java.math.BigDecimal;
import org.springframework.stereotype.Service;


import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionId;
import io.rashed.finance.domain.transactions.TransactionRepository;



@Service
public class UpdateTransactionService {



    private final TransactionRepository repository;



    public UpdateTransactionService(
            TransactionRepository repository
    ) {

        this.repository = repository;

    }




    public Transaction execute(
            UpdateTransactionCommand command
    ) {



        Transaction existing =
                repository.findById(
                        command.transactionId()
                )
                .orElseThrow(
                        () ->
                        new ResourceNotFoundException(
                                "Transaction not found."
                        )
                );



        if(existing.isVoided()) {

            throw new IllegalStateException(
                    "Voided transaction cannot be updated."
            );

        }



        if(existing.isReversed()) {

            throw new IllegalStateException(
                    "Reversed transaction cannot be updated."
            );

        }



        /*
         *
         * Calculate difference
         *
         */

        var difference =
                command.newAmount()
                .subtract(
                        existing.getAmount()
                );



        if(difference.isZero()) {

            return existing;

        }



        Transaction adjustment =
                Transaction.adjustment(
                        TransactionId.newId(),
                        existing.getTransactionDate(),
                        difference.abs(),
                        existing.getFromAccountId(),
                        existing.getId(),
                        AdjustmentReason.TRANSACTION_UPDATE,
                        "Transaction update adjustment"
                );



        return repository.save(adjustment);

    }

}