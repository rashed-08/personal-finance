package io.rashed.finance.application.auth;

import io.rashed.finance.common.exception.InvalidCredentialsException;
import io.rashed.finance.domain.users.RefreshToken;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import io.rashed.finance.infrastructure.security.JwtProperties;
import io.rashed.finance.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private LoginService service;

    private User localUser;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);

        JwtService jwtService = new JwtService(new JwtProperties(
                "test-secret-key-that-is-long-enough-0123456789",
                "personal-finance",
                Duration.ofMinutes(15),
                Duration.ofDays(14)
        ));

        IssueTokensService issueTokensService =
                new IssueTokensService(refreshTokenRepository, jwtService, Duration.ofDays(14));

        service = new LoginService(userRepository, passwordEncoder, issueTokensService);

        localUser = User.registerLocal(
                "login@example.com",
                passwordEncoder.encode("correct-password"),
                "Login User"
        );

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void login_returnsTokensForValidCredentials() {

        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(localUser));

        AuthResult result = service.execute(new LoginCommand("login@example.com", "correct-password"));

        assertNotNull(result.accessToken());
        assertNotNull(result.refreshToken());
        assertEquals(localUser, result.user());
    }

    @Test
    void login_storesOnlyHashOfRefreshToken() {

        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(localUser));

        AuthResult result = service.execute(new LoginCommand("login@example.com", "correct-password"));

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        assertNotEquals(result.refreshToken(), captor.getValue().getTokenHash());
        assertEquals(TokenHasher.sha256Hex(result.refreshToken()), captor.getValue().getTokenHash());
    }

    @Test
    void login_rejectsWrongPassword() {

        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(localUser));

        assertThrows(InvalidCredentialsException.class, () ->
                service.execute(new LoginCommand("login@example.com", "wrong-password")));
    }

    @Test
    void login_rejectsUnknownEmail() {

        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () ->
                service.execute(new LoginCommand("nobody@example.com", "whatever")));
    }

    @Test
    void login_rejectsGoogleOnlyUser() {

        User googleUser = User.registerWithGoogle(
                "google@example.com", "Google User", "google-sub-123", true);

        when(userRepository.findByEmail("google@example.com")).thenReturn(Optional.of(googleUser));

        assertThrows(InvalidCredentialsException.class, () ->
                service.execute(new LoginCommand("google@example.com", "anything")));
    }
}
