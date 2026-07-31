package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.RefreshToken;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LogoutServiceTest {

    private RefreshTokenRepository refreshTokenRepository;
    private LogoutService service;

    @BeforeEach
    void setUp() {

        refreshTokenRepository = mock(RefreshTokenRepository.class);
        service = new LogoutService(refreshTokenRepository);
    }

    @Test
    void logout_revokesPresentedToken() {

        RefreshToken token = RefreshToken.issue(
                UserId.newId(),
                TokenHasher.sha256Hex("raw-token"),
                LocalDateTime.now().plusDays(14)
        );

        when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex("raw-token")))
                .thenReturn(Optional.of(token));

        service.execute("raw-token");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        assertTrue(captor.getValue().isRevoked());
    }

    @Test
    void logout_isIdempotentForMissingToken() {

        assertDoesNotThrow(() -> service.execute(null));
        assertDoesNotThrow(() -> service.execute(""));

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void logout_isIdempotentForUnknownToken() {

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.execute("unknown"));
    }
}
