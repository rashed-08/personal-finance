package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Revokes the presented refresh token. Idempotent: logging out with a
 * missing, unknown or already-revoked token succeeds silently.
 */
@Service
public final class LogoutService {

    private final RefreshTokenRepository refreshTokenRepository;

    public LogoutService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository =
                Objects.requireNonNull(refreshTokenRepository, "RefreshTokenRepository cannot be null.");
    }

    public void execute(String rawRefreshToken) {

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        refreshTokenRepository
                .findByTokenHash(TokenHasher.sha256Hex(rawRefreshToken))
                .ifPresent(token -> refreshTokenRepository.save(token.revoke()));
    }
}
