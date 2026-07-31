package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.PasswordResetToken;
import io.rashed.finance.domain.users.PasswordResetTokenRepository;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResetPasswordServiceTest {

    private static final String RAW_TOKEN = "raw-reset-token";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private PasswordResetTokenRepository tokenRepository;
    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private ResetPasswordService service;

    private User user;

    @BeforeEach
    void setUp() {

        tokenRepository = mock(PasswordResetTokenRepository.class);
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);

        service = new ResetPasswordService(
                tokenRepository, userRepository, refreshTokenRepository, passwordEncoder);

        user = User.registerLocal(
                "reset@example.com",
                passwordEncoder.encode("old-password"),
                "Reset User"
        );

        when(tokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void reset_changesPasswordConsumesTokenAndRevokesSessions() {

        PasswordResetToken token = PasswordResetToken.issue(
                user.getId(),
                TokenHasher.sha256Hex(RAW_TOKEN),
                LocalDateTime.now().plusHours(1)
        );

        when(tokenRepository.findByTokenHash(TokenHasher.sha256Hex(RAW_TOKEN)))
                .thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.execute(RAW_TOKEN, "new-password-123");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(passwordEncoder.matches("new-password-123", userCaptor.getValue().getPasswordHash()));

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertTrue(tokenCaptor.getValue().isUsed());

        verify(refreshTokenRepository).revokeAllForUser(user.getId());
    }

    @Test
    void reset_rejectsUnknownToken() {

        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.execute("unknown", "new-password-123"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void reset_rejectsUsedToken() {

        PasswordResetToken used = PasswordResetToken.issue(
                user.getId(),
                TokenHasher.sha256Hex(RAW_TOKEN),
                LocalDateTime.now().plusHours(1)
        ).markUsed();

        when(tokenRepository.findByTokenHash(TokenHasher.sha256Hex(RAW_TOKEN)))
                .thenReturn(Optional.of(used));

        assertThrows(IllegalArgumentException.class,
                () -> service.execute(RAW_TOKEN, "new-password-123"));

        verify(userRepository, never()).save(any());
    }
}
