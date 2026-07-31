package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.PasswordResetToken;
import io.rashed.finance.domain.users.PasswordResetTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ForgotPasswordServiceTest {

    private PasswordResetTokenRepository tokenRepository;
    private UserRepository userRepository;
    private EmailSender emailSender;
    private ForgotPasswordService service;

    @BeforeEach
    void setUp() {

        tokenRepository = mock(PasswordResetTokenRepository.class);
        userRepository = mock(UserRepository.class);
        emailSender = mock(EmailSender.class);

        service = new ForgotPasswordService(
                tokenRepository,
                userRepository,
                emailSender,
                Duration.ofHours(1),
                "http://localhost:5173"
        );

        when(tokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void forgot_issuesTokenAndSendsResetLink() {

        User user = User.registerLocal("forgot@example.com", "hash", "Forgot User");

        when(userRepository.findByEmail("forgot@example.com")).thenReturn(Optional.of(user));

        service.execute("forgot@example.com");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(captor.capture());
        assertTrue(captor.getValue().isUsable());

        verify(emailSender).send(
                eq("forgot@example.com"),
                anyString(),
                contains("/reset-password?token=")
        );
    }

    @Test
    void forgot_isSilentForUnknownEmail() {

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> service.execute("nobody@example.com"));

        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void forgot_isSilentForGoogleOnlyAccount() {

        User googleUser = User.registerWithGoogle(
                "google@example.com", "Google User", "sub-1", true);

        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(googleUser));

        assertDoesNotThrow(() -> service.execute("google@example.com"));

        verifyNoInteractions(tokenRepository, emailSender);
    }
}
