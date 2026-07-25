package io.rashed.finance.application.transaction;


import org.springframework.stereotype.Service;


import io.rashed.finance.common.enums.AdjustmentReason;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.accounts.AccountId;
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
         * Apply description/category/notes changes in place.
         * Amount corrections are never rewritten in place; they are
         * recorded as a separate adjustment so history stays traceable.
         *
         */

        Transaction updated =
                existing.withDetails(
                        command.newCategoryId(),
                        command.description(),
                        command.notes()
                );

        Transaction saved = repository.save(updated);



        var difference =
                command.newAmount()
                .subtract(
                        existing.getAmount()
                );



        if(difference.isZero()) {

            return saved;

        }



        if(existing.isTransfer()) {

            throw new IllegalStateException(
                    "Transfer amount cannot be corrected via update; void this transaction and record a new one instead."
            );

        }



        /*
         *
         * Whether the correction should add to or subtract from the
         * affected account depends on both the direction the difference
         * moves in AND whether the original transaction increases or
         * decreases that account's balance.
         *
         */

        boolean amountGrew = difference.isPositive();

        boolean adjustmentIncreasesBalance =
                existing.increasesBalance() == amountGrew;

        AccountId affectedAccountId = existing.affectedAccountId();

        Transaction adjustment =
                Transaction.adjustment(
                        TransactionId.newId(),
                        existing.getTransactionDate(),
                        difference.abs(),
                        adjustmentIncreasesBalance ? null : affectedAccountId,
                        adjustmentIncreasesBalance ? affectedAccountId : null,
                        existing.getId(),
                        AdjustmentReason.TRANSACTION_UPDATE,
                        "Adjustment for transaction update (" + existing.getId().getValue() + ")",
                        null
                );



        repository.save(adjustment);



        return saved;

    }

}