package io.rashed.finance.application.auth;

import io.rashed.finance.common.enums.AuthProvider;
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

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GoogleSignInServiceTest {

    private static final String ID_TOKEN = "google-id-token";
    private static final String SUBJECT = "google-sub-123";

    private GoogleTokenVerifier googleTokenVerifier;
    private UserRepository userRepository;
    private GoogleSignInService service;

    @BeforeEach
    void setUp() {

        googleTokenVerifier = mock(GoogleTokenVerifier.class);
        userRepository = mock(UserRepository.class);
        RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);

        JwtService jwtService = new JwtService(new JwtProperties(
                "test-secret-key-that-is-long-enough-0123456789",
                "personal-finance",
                Duration.ofMinutes(15),
                Duration.ofDays(14)
        ));

        IssueTokensService issueTokensService =
                new IssueTokensService(refreshTokenRepository, jwtService, Duration.ofDays(14));

        service = new GoogleSignInService(googleTokenVerifier, userRepository, issueTokensService);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void signIn_returnsTokensForKnownGoogleUser() {

        User existing = User.registerWithGoogle("g@example.com", "G User", SUBJECT, true);

        when(googleTokenVerifier.verify(ID_TOKEN))
                .thenReturn(Optional.of(new GoogleUserInfo(SUBJECT, "g@example.com", true, "G User")));
        when(userRepository.findByProviderSubject(SUBJECT)).thenReturn(Optional.of(existing));

        AuthResult result = service.execute(ID_TOKEN);

        assertNotNull(result.accessToken());
        assertEquals(existing, result.user());
        verify(userRepository, never()).save(any());
    }

    @Test
    void signIn_registersNewUserOnFirstSignIn() {

        when(googleTokenVerifier.verify(ID_TOKEN))
                .thenReturn(Optional.of(new GoogleUserInfo(SUBJECT, "new@example.com", true, "New User")));
        when(userRepository.findByProviderSubject(SUBJECT)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        AuthResult result = service.execute(ID_TOKEN);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User created = captor.getValue();
        assertEquals("new@example.com", created.getEmail());
        assertEquals(AuthProvider.GOOGLE, created.getProvider());
        assertEquals(SUBJECT, created.getProviderSubject());
        assertTrue(created.isEmailVerified());
        assertNotNull(result.refreshToken());
    }

    @Test
    void signIn_linksGoogleIdentityToExistingLocalAccount() {

        User local = User.registerLocal("local@example.com", "hash", "Local User");

        when(googleTokenVerifier.verify(ID_TOKEN))
                .thenReturn(Optional.of(new GoogleUserInfo(SUBJECT, "local@example.com", true, "Local User")));
        when(userRepository.findByProviderSubject(SUBJECT)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(local));

        AuthResult result = service.execute(ID_TOKEN);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User linked = captor.getValue();
        assertEquals(local.getId(), linked.getId());
        assertEquals(SUBJECT, linked.getProviderSubject());
        assertTrue(linked.hasLocalPassword(), "linking must not drop the local password");
        assertNotNull(result.accessToken());
    }

    @Test
    void signIn_rejectsInvalidToken() {

        when(googleTokenVerifier.verify(ID_TOKEN)).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> service.execute(ID_TOKEN));
    }

    @Test
    void signIn_rejectsUnverifiedGoogleEmail() {

        when(googleTokenVerifier.verify(ID_TOKEN))
                .thenReturn(Optional.of(new GoogleUserInfo(SUBJECT, "sketchy@example.com", false, "Sketchy")));

        assertThrows(InvalidCredentialsException.class, () -> service.execute(ID_TOKEN));

        verify(userRepository, never()).save(any());
    }
}
