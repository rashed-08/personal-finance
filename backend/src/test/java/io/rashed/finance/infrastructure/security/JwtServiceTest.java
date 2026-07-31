package io.rashed.finance.infrastructure.security;

import io.rashed.finance.common.enums.UserRole;
import io.rashed.finance.domain.users.User;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-0123456789";

    private final User user = User.registerLocal("jwt@example.com", "hash", "Jwt User");

    private JwtService service(Duration accessTtl, String issuer) {
        return new JwtService(new JwtProperties(SECRET, issuer, accessTtl, Duration.ofDays(14)));
    }

    @Test
    void parse_returnsPrincipalFromValidToken() {

        JwtService service = service(Duration.ofMinutes(15), "personal-finance");

        String token = service.createAccessToken(user);

        Optional<AuthenticatedUser> parsed = service.parse(token);

        assertTrue(parsed.isPresent());
        assertEquals(user.getId().getValue(), parsed.get().id());
        assertEquals("jwt@example.com", parsed.get().email());
        assertEquals(UserRole.OWNER, parsed.get().role());
    }

    @Test
    void parse_rejectsTamperedToken() {

        JwtService service = service(Duration.ofMinutes(15), "personal-finance");

        String token = service.createAccessToken(user);

        assertTrue(service.parse(token + "x").isEmpty());
        assertTrue(service.parse("not-a-jwt").isEmpty());
    }

    @Test
    void parse_rejectsExpiredToken() {

        JwtService expiredIssuer = service(Duration.ofMinutes(-5), "personal-finance");

        String token = expiredIssuer.createAccessToken(user);

        assertTrue(expiredIssuer.parse(token).isEmpty());
    }

    @Test
    void parse_rejectsWrongIssuer() {

        JwtService otherIssuer = service(Duration.ofMinutes(15), "someone-else");
        JwtService service = service(Duration.ofMinutes(15), "personal-finance");

        String token = otherIssuer.createAccessToken(user);

        assertTrue(service.parse(token).isEmpty());
    }

    @Test
    void constructor_rejectsShortSecret() {

        assertThrows(IllegalStateException.class, () ->
                new JwtService(new JwtProperties(
                        "too-short",
                        "personal-finance",
                        Duration.ofMinutes(15),
                        Duration.ofDays(14)
                ))
        );
    }
}
