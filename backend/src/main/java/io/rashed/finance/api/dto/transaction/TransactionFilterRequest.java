package io.rashed.finance.api.dto.transaction;


import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;

import java.time.LocalDate;
import java.util.UUID;


public record TransactionFilterRequest(

        LocalDate fromDate,

        LocalDate toDate,

        TransactionType transactionType,

        TransactionStatus transactionStatus,

        UUID accountId,

        UUID categoryId,

        UUID salaryCycleId,

        UUID fundId

) {
}