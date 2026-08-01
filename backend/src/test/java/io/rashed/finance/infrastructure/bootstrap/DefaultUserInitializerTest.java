package io.rashed.finance.infrastructure.bootstrap;

import io.rashed.finance.common.enums.UserRole;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultUserInitializerTest {

    private static final String EMAIL = "owner@personal-finance.local";
    private static final String PASSWORD = "password123";

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserRepository userRepository;
    private DefaultUserInitializer initializer;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);

        initializer = new DefaultUserInitializer(
                userRepository, passwordEncoder, EMAIL, PASSWORD, "Default Owner");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void run_createsPreVerifiedOwnerWithHashedPassword() {

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

        initializer.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User created = captor.getValue();
        assertEquals(EMAIL, created.getEmail());
        assertEquals(UserRole.OWNER, created.getRole());
        assertTrue(created.isEmailVerified(), "must skip the email round-trip");
        assertTrue(passwordEncoder.matches(PASSWORD, created.getPasswordHash()));
    }

    @Test
    void run_isIdempotent() {

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        initializer.run(null);

        // Restarting must not overwrite a password the developer changed.
        verify(userRepository, never()).save(any(User.class));
    }
}
