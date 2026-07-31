package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.EmailVerificationToken;
import io.rashed.finance.domain.users.EmailVerificationTokenId;
import io.rashed.finance.domain.users.EmailVerificationTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerifyEmailServiceTest {

    private static final String RAW_TOKEN = "raw-verification-token";

    private EmailVerificationTokenRepository tokenRepository;
    private UserRepository userRepository;
    private VerifyEmailService service;

    private User user;

    @BeforeEach
    void setUp() {

        tokenRepository = mock(EmailVerificationTokenRepository.class);
        userRepository = mock(UserRepository.class);

        service = new VerifyEmailService(tokenRepository, userRepository);

        user = User.registerLocal("verify@example.com", "hash", "Verify User");

        when(tokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void verify_marksEmailVerifiedAndConsumesToken() {

        EmailVerificationToken token = EmailVerificationToken.issue(
                user.getId(),
                TokenHasher.sha256Hex(RAW_TOKEN),
                LocalDateTime.now().plusHours(24)
        );

        when(tokenRepository.findByTokenHash(TokenHasher.sha256Hex(RAW_TOKEN)))
                .thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.execute(RAW_TOKEN);

        ArgumentCaptor<EmailVerificationToken> tokenCaptor =
                ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        assertTrue(tokenCaptor.getValue().isUsed());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().isEmailVerified());
    }

    @Test
    void verify_rejectsUnknownToken() {

        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.execute("unknown"));
    }

    @Test
    void verify_rejectsUsedToken() {

        EmailVerificationToken used = EmailVerificationToken.issue(
                user.getId(),
                TokenHasher.sha256Hex(RAW_TOKEN),
                LocalDateTime.now().plusHours(24)
        ).markUsed();

        when(tokenRepository.findByTokenHash(TokenHasher.sha256Hex(RAW_TOKEN)))
                .thenReturn(Optional.of(used));

        assertThrows(IllegalArgumentException.class, () -> service.execute(RAW_TOKEN));
    }

    @Test
    void verify_rejectsExpiredToken() {

        EmailVerificationToken expired = new EmailVerificationToken(
                EmailVerificationTokenId.newId(),
                user.getId(),
                TokenHasher.sha256Hex(RAW_TOKEN),
                LocalDateTime.now().minusMinutes(1),
                null,
                LocalDateTime.now().minusHours(25)
        );

        when(tokenRepository.findByTokenHash(TokenHasher.sha256Hex(RAW_TOKEN)))
                .thenReturn(Optional.of(expired));

        assertThrows(IllegalArgumentException.class, () -> service.execute(RAW_TOKEN));
    }
}
