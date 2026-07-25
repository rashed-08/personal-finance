package io.rashed.finance.application.report;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import io.rashed.finance.common.enums.TransactionStatus;
import io.rashed.finance.common.enums.TransactionType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.transactions.Transaction;
import io.rashed.finance.domain.transactions.TransactionFilter;
import io.rashed.finance.domain.transactions.TransactionRepository;

@Service
public class GetCashFlowReportService {

    private final TransactionRepository transactionRepository;

    public GetCashFlowReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
    }

    public CashFlowReportResult execute(LocalDate fromDate, LocalDate toDate) {

        Money moneyIn = sumByType(TransactionType.INCOME, fromDate, toDate);
        Money moneyOut = sumByType(TransactionType.EXPENSE, fromDate, toDate);
        Money transferVolume = sumByType(TransactionType.TRANSFER, fromDate, toDate);

        return new CashFlowReportResult(fromDate, toDate, moneyIn, moneyOut, moneyIn.subtract(moneyOut), transferVolume);
    }

    private Money sumByType(TransactionType type, LocalDate fromDate, LocalDate toDate) {

        TransactionFilter filter = new TransactionFilter(
                fromDate, toDate, type, TransactionStatus.POSTED, null, null, null, null, null);

        Money total = Money.zero();

        for (Transaction transaction : transactionRepository.find(filter, Pageable.unpaged())) {
            total = total.add(transaction.getAmount());
        }

        return total;
    }
}
