package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.EmailVerificationToken;
import io.rashed.finance.domain.users.EmailVerificationTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SendEmailVerificationServiceTest {

    private EmailVerificationTokenRepository tokenRepository;
    private UserRepository userRepository;
    private EmailSender emailSender;
    private SendEmailVerificationService service;

    @BeforeEach
    void setUp() {

        tokenRepository = mock(EmailVerificationTokenRepository.class);
        userRepository = mock(UserRepository.class);
        emailSender = mock(EmailSender.class);

        service = new SendEmailVerificationService(
                tokenRepository,
                userRepository,
                emailSender,
                Duration.ofHours(24),
                "http://localhost:5173"
        );

        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void send_issuesTokenAndEmailsVerificationLink() {

        User user = User.registerLocal("send@example.com", "hash", "Send User");

        service.sendFor(user);

        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailSender).send(
                eq("send@example.com"),
                anyString(),
                contains("/verify-email?token=")
        );
    }

    @Test
    void send_skipsAlreadyVerifiedUser() {

        User verified = User.registerLocal("done@example.com", "hash", "Done User").verifyEmail();

        service.sendFor(verified);

        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void resend_isSilentForUnknownEmail() {

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        service.resend("nobody@example.com");

        verifyNoInteractions(tokenRepository, emailSender);
    }
}
