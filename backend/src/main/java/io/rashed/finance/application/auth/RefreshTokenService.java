package io.rashed.finance.application.auth;

import io.rashed.finance.common.exception.InvalidRefreshTokenException;
import io.rashed.finance.domain.users.RefreshToken;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Rotates a refresh token: the presented token is revoked and a new
 * token pair is issued.
 *
 * Not final: {@code @Transactional} is applied through a CGLIB proxy,
 * which cannot subclass a final class.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final IssueTokensService issueTokensService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            IssueTokensService issueTokensService
    ) {
        this.refreshTokenRepository =
                Objects.requireNonNull(refreshTokenRepository, "RefreshTokenRepository cannot be null.");
        this.userRepository =
                Objects.requireNonNull(userRepository, "UserRepository cannot be null.");
        this.issueTokensService =
                Objects.requireNonNull(issueTokensService, "IssueTokensService cannot be null.");
    }

    /**
     * {@code noRollbackFor}: the reuse-detection branch revokes every session
     * and then fails the request. A rollback would undo that revocation, so
     * the theft response has to survive the exception.
     */
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthResult execute(String rawRefreshToken) {

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token is missing.");
        }

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(TokenHasher.sha256Hex(rawRefreshToken))
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token is not recognized."));

        if (refreshToken.isRevoked()) {
            // Reuse of a rotated token indicates the token may have been
            // stolen — revoke every active session for this user.
            refreshTokenRepository.revokeAllForUser(refreshToken.getUserId());

            throw new InvalidRefreshTokenException("Refresh token has been revoked.");
        }

        if (refreshToken.isExpired()) {
            throw new InvalidRefreshTokenException("Refresh token has expired.");
        }

        refreshTokenRepository.save(refreshToken.revoke());

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new InvalidRefreshTokenException("User no longer exists."));

        return issueTokensService.execute(user);
    }
}
