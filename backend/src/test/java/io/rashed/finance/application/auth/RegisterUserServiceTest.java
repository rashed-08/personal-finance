package io.rashed.finance.application.auth;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterUserServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private RegisterUserService service;

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

        service = new RegisterUserService(userRepository, passwordEncoder, issueTokensService);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void register_createsUserWithHashedPasswordAndIssuesTokens() {

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        AuthResult result = service.execute(
                new RegisterUserCommand("new@example.com", "password123", "New User"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("new@example.com", saved.getEmail());
        assertNotEquals("password123", saved.getPasswordHash());
        assertTrue(passwordEncoder.matches("password123", saved.getPasswordHash()));

        assertNotNull(result.accessToken());
        assertNotNull(result.refreshToken());
    }

    @Test
    void register_normalizesEmailToLowerCase() {

        when(userRepository.existsByEmail(any())).thenReturn(false);

        AuthResult result = service.execute(
                new RegisterUserCommand("Mixed.Case@Example.COM", "password123", "New User"));

        assertEquals("mixed.case@example.com", result.user().getEmail());
    }

    @Test
    void register_rejectsDuplicateEmail() {

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.execute(new RegisterUserCommand("taken@example.com", "password123", "New User")));
    }
}
