package io.rashed.finance.api.dto.transaction;


import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.salarycycle.SalaryCycleId;
import io.rashed.finance.domain.transactions.TransactionFilter;


public final class TransactionFilterMapper {


    private TransactionFilterMapper() {
    }


    public static TransactionFilter toDomain(
            TransactionFilterRequest request
    ) {

        return new TransactionFilter(

                request.fromDate(),

                request.toDate(),

                request.transactionType(),

                request.transactionStatus(),

                request.accountId() == null
                        ? null
                        : AccountId.of(request.accountId()),


                request.categoryId() == null
                        ? null
                        : CategoryId.of(request.categoryId()),


                request.salaryCycleId() == null
                        ? null
                        : SalaryCycleId.of(request.salaryCycleId())
        );
    }
}