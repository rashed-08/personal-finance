package io.rashed.finance.application.report;

import io.rashed.finance.application.account.CalculateAccountBalanceService;
import io.rashed.finance.common.enums.AccountType;
import io.rashed.finance.common.valueobject.Money;
import io.rashed.finance.domain.accounts.Account;
import io.rashed.finance.domain.accounts.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAccountBalancesReportServiceTest {

    private final LocalDate asOfDate = LocalDate.of(2026, 7, 25);

    private AccountRepository accountRepository;
    private CalculateAccountBalanceService calculateAccountBalanceService;
    private GetAccountBalancesReportService service;

    @BeforeEach
    void setUp() {

        accountRepository = mock(AccountRepository.class);
        calculateAccountBalanceService = mock(CalculateAccountBalanceService.class);
        service = new GetAccountBalancesReportService(accountRepository, calculateAccountBalanceService);
    }

    @Test
    void execute_returnsBalancePerAccount() {

        Account cash = Account.create("Cash", AccountType.CASH, Money.zero(), null);
        Account bank = Account.create("Bank", AccountType.BANK, Money.zero(), null);

        when(accountRepository.findActive()).thenReturn(List.of(cash, bank));
        when(calculateAccountBalanceService.execute(eq(cash.getId()), any())).thenReturn(Money.of(1000));
        when(calculateAccountBalanceService.execute(eq(bank.getId()), any())).thenReturn(Money.of(5000));

        List<AccountBalance> result = service.execute(asOfDate, true);

        assertEquals(2, result.size());
        assertEquals(Money.of(1000), result.get(0).balance());
        assertEquals(Money.of(5000), result.get(1).balance());
    }
}
