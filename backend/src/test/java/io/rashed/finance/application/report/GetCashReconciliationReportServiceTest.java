package io.rashed.finance.application.report;

import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class GetCashReconciliationReportServiceTest {

    private final AccountId accountId = AccountId.newId();
    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private CashReconciliationRepository repository;
    private GetCashReconciliationReportService service;

    @BeforeEach
    void setUp() {

        repository = mock(CashReconciliationRepository.class);
        service = new GetCashReconciliationReportService(repository);
    }

    @Test
    void execute_filtersByAccountWhenGiven() {

        CashReconciliation reconciliation = CashReconciliation.start(accountId, today, Money.of(500), null);
        when(repository.findByAccount(accountId)).thenReturn(List.of(reconciliation));

        List<CashReconciliation> result = service.execute(accountId, null, null);

        assertEquals(1, result.size());
        verify(repository).findByAccount(accountId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void execute_filtersByDateRangeWhenNoAccountGiven() {

        when(repository.findByDateRange(today, today)).thenReturn(List.of());

        service.execute(null, today, today);

        verify(repository).findByDateRange(today, today);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void execute_returnsAllWhenNoFilterGiven() {

        when(repository.findAll()).thenReturn(List.of());

        service.execute(null, null, null);

        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }
}
