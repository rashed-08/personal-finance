package io.rashed.finance.application.transaction;


import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.categories.CategoryId;
import io.rashed.finance.domain.transactions.TransactionId;



public record UpdateTransactionCommand(

        TransactionId transactionId,

        Money newAmount,

        CategoryId newCategoryId,

        String description,

        String notes

) {

}