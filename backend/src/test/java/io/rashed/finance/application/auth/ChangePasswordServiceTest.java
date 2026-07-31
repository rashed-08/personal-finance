package io.rashed.finance.application.auth;

import io.rashed.finance.common.exception.InvalidCredentialsException;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChangePasswordServiceTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private ChangePasswordService service;

    private User user;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);

        service = new ChangePasswordService(userRepository, refreshTokenRepository, passwordEncoder);

        user = User.registerLocal(
                "change@example.com",
                passwordEncoder.encode("current-password"),
                "Change User"
        );

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void change_updatesPasswordAndRevokesSessions() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        service.execute(user.getId(), "current-password", "brand-new-password");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertTrue(passwordEncoder.matches("brand-new-password", captor.getValue().getPasswordHash()));

        verify(refreshTokenRepository).revokeAllForUser(user.getId());
    }

    @Test
    void change_rejectsWrongCurrentPassword() {

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class,
                () -> service.execute(user.getId(), "wrong-password", "brand-new-password"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void change_rejectsGoogleOnlyAccount() {

        User googleUser = User.registerWithGoogle(
                "google@example.com", "Google User", "sub-1", true);

        when(userRepository.findById(googleUser.getId())).thenReturn(Optional.of(googleUser));

        assertThrows(IllegalArgumentException.class,
                () -> service.execute(googleUser.getId(), "anything", "brand-new-password"));

        verify(userRepository, never()).save(any());
    }
}
