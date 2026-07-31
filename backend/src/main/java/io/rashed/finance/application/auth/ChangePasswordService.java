package io.rashed.finance.application.auth;

import io.rashed.finance.common.exception.InvalidCredentialsException;
import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.users.RefreshTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Changes the password of an authenticated user. Requires the current
 * password and revokes every refresh token afterwards, ending all
 * sessions (the client re-authenticates with the new password).
 */
@Service
public final class ChangePasswordService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository =
                Objects.requireNonNull(userRepository, "UserRepository cannot be null.");
        this.refreshTokenRepository =
                Objects.requireNonNull(refreshTokenRepository, "RefreshTokenRepository cannot be null.");
        this.passwordEncoder =
                Objects.requireNonNull(passwordEncoder, "PasswordEncoder cannot be null.");
    }

    @Transactional
    public void execute(UserId userId, String currentPassword, String newPassword) {

        Objects.requireNonNull(userId, "UserId cannot be null.");
        Objects.requireNonNull(newPassword, "New password cannot be null.");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId.asString()));

        if (!user.hasLocalPassword()) {
            throw new IllegalArgumentException(
                    "This account has no local password. Use the forgot-password flow to set one."
            );
        }

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        userRepository.save(user.changePassword(passwordEncoder.encode(newPassword)));

        refreshTokenRepository.revokeAllForUser(user.getId());
    }
}
