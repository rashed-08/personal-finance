package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.RefreshToken;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.infrastructure.security.JwtService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;

/**
 * Issues a fresh access + refresh token pair for a user.
 */
@Service
public final class IssueTokensService {

    private static final int REFRESH_TOKEN_BYTES = 32;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final Duration refreshTokenTtl;

    private final SecureRandom secureRandom = new SecureRandom();

    public IssueTokensService(
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            @Value("${app.security.jwt.refresh-token-ttl}") Duration refreshTokenTtl
    ) {
        this.refreshTokenRepository =
                Objects.requireNonNull(refreshTokenRepository, "RefreshTokenRepository cannot be null.");
        this.jwtService =
                Objects.requireNonNull(jwtService, "JwtService cannot be null.");
        this.refreshTokenTtl =
                Objects.requireNonNull(refreshTokenTtl, "Refresh token TTL cannot be null.");
    }

    public AuthResult execute(User user) {

        Objects.requireNonNull(user, "User cannot be null.");

        String rawRefreshToken = generateRawToken();

        RefreshToken refreshToken = RefreshToken.issue(
                user.getId(),
                TokenHasher.sha256Hex(rawRefreshToken),
                LocalDateTime.now().plus(refreshTokenTtl)
        );

        refreshTokenRepository.save(refreshToken);

        return new AuthResult(
                user,
                jwtService.createAccessToken(user),
                jwtService.accessTokenTtlSeconds(),
                rawRefreshToken,
                refreshTokenTtl
        );
    }

    private String generateRawToken() {

        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
