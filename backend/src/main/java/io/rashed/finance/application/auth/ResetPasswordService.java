package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.PasswordResetToken;
import io.rashed.finance.domain.users.PasswordResetTokenRepository;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Consumes a password-reset token, sets the new password, and revokes
 * every refresh token so all sessions must re-authenticate.
 */
@Service
public final class ResetPasswordService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public ResetPasswordService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.tokenRepository =
                Objects.requireNonNull(tokenRepository, "PasswordResetTokenRepository cannot be null.");
        this.userRepository =
                Objects.requireNonNull(userRepository, "UserRepository cannot be null.");
        this.refreshTokenRepository =
                Objects.requireNonNull(refreshTokenRepository, "RefreshTokenRepository cannot be null.");
        this.passwordEncoder =
                Objects.requireNonNull(passwordEncoder, "PasswordEncoder cannot be null.");
    }

    @Transactional
    public void execute(String rawToken, String newPassword) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Invalid or expired reset token.");
        }

        Objects.requireNonNull(newPassword, "New password cannot be null.");

        PasswordResetToken token = tokenRepository
                .findByTokenHash(TokenHasher.sha256Hex(rawToken))
                .filter(PasswordResetToken::isUsable)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token."));

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token."));

        tokenRepository.save(token.markUsed());
        userRepository.save(user.changePassword(passwordEncoder.encode(newPassword)));

        refreshTokenRepository.revokeAllForUser(user.getId());
    }
}
