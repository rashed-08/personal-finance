package io.rashed.finance.application.reconciliation;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.AccountId;
import io.rashed.finance.domain.reconciliation.CashReconciliation;
import io.rashed.finance.domain.reconciliation.CashReconciliationId;
import io.rashed.finance.domain.reconciliation.CashReconciliationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RecordCashSnapshotServiceTest {

    private final LocalDate today = LocalDate.of(2026, 7, 25);

    private CashReconciliationRepository repository;
    private RecordCashSnapshotService service;

    @BeforeEach
    void setUp() {

        repository = mock(CashReconciliationRepository.class);
        service = new RecordCashSnapshotService(repository);

        when(repository.save(any(CashReconciliation.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void execute_appendsASnapshotToTheReconciliation() {

        CashReconciliation reconciliation = CashReconciliation.start(AccountId.newId(), today, Money.of(2200), null);
        when(repository.findById(reconciliation.getId())).thenReturn(Optional.of(reconciliation));

        CashReconciliation updated = service.execute(reconciliation.getId(), Money.of(1940), "count");

        assertEquals(1, updated.getSnapshots().size());
        assertEquals(Money.of(1940), updated.getActualCashAmount());
    }

    @Test
    void execute_rejectsUnknownReconciliation() {

        CashReconciliationId id = CashReconciliationId.newId();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute(id, Money.of(100), null));
    }
}
