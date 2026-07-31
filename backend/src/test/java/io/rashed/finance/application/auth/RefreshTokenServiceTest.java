package io.rashed.finance.application.auth;

import io.rashed.finance.common.exception.InvalidRefreshTokenException;
import io.rashed.finance.domain.users.RefreshToken;
import io.rashed.finance.domain.users.RefreshTokenId;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import io.rashed.finance.infrastructure.security.JwtProperties;
import io.rashed.finance.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private static final String RAW_TOKEN = "raw-refresh-token";

    private RefreshTokenRepository refreshTokenRepository;
    private UserRepository userRepository;
    private RefreshTokenService service;

    private User user;

    @BeforeEach
    void setUp() {

        refreshTokenRepository = mock(RefreshTokenRepository.class);
        userRepository = mock(UserRepository.class);

        JwtService jwtService = new JwtService(new JwtProperties(
                "test-secret-key-that-is-long-enough-0123456789",
                "personal-finance",
                Duration.ofMinutes(15),
                Duration.ofDays(14)
        ));

        IssueTokensService issueTokensService =
                new IssueTokensService(refreshTokenRepository, jwtService, Duration.ofDays(14));

        service = new RefreshTokenService(refreshTokenRepository, userRepository, issueTokensService);

        user = User.registerLocal("refresh@example.com", "hash", "Refresh User");

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private RefreshToken activeToken() {
        return RefreshToken.issue(
                user.getId(),
                TokenHasher.sha256Hex(RAW_TOKEN),
                LocalDateTime.now().plusDays(14)
        );
    }

    @Test
    void refresh_rotatesTokenAndIssuesNewPair() {

        when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(RAW_TOKEN)))
                .thenReturn(Optional.of(activeToken()));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        AuthResult result = service.execute(RAW_TOKEN);

        assertNotNull(result.accessToken());
        assertNotEquals(RAW_TOKEN, result.refreshToken());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, atLeastOnce()).save(captor.capture());

        List<RefreshToken> saved = captor.getAllValues();
        assertEquals(2, saved.size());
        assertTrue(saved.get(0).isRevoked(), "presented token must be revoked");
        assertTrue(saved.get(1).isActive(), "replacement token must be active");
    }

    @Test
    void refresh_rejectsMissingToken() {

        assertThrows(InvalidRefreshTokenException.class, () -> service.execute(null));
        assertThrows(InvalidRefreshTokenException.class, () -> service.execute("  "));
    }

    @Test
    void refresh_rejectsUnknownToken() {

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class, () -> service.execute("unknown-token"));
    }

    @Test
    void refresh_reuseOfRevokedTokenRevokesAllSessions() {

        RefreshToken revoked = activeToken().revoke();

        when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(RAW_TOKEN)))
                .thenReturn(Optional.of(revoked));

        assertThrows(InvalidRefreshTokenException.class, () -> service.execute(RAW_TOKEN));

        verify(refreshTokenRepository).revokeAllForUser(user.getId());
    }

    @Test
    void refresh_rejectsExpiredToken() {

        RefreshToken expired = new RefreshToken(
                RefreshTokenId.newId(),
                user.getId(),
                TokenHasher.sha256Hex(RAW_TOKEN),
                LocalDateTime.now().minusMinutes(1),
                null,
                LocalDateTime.now().minusDays(15)
        );

        when(refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(RAW_TOKEN)))
                .thenReturn(Optional.of(expired));

        assertThrows(InvalidRefreshTokenException.class, () -> service.execute(RAW_TOKEN));
    }
}
