package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.EmailVerificationToken;
import io.rashed.finance.domain.users.EmailVerificationTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Consumes an email-verification token and marks the user's email verified.
 *
 * Not final: {@code @Transactional} is applied through a CGLIB proxy,
 * which cannot subclass a final class.
 */
@Service
public class VerifyEmailService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public VerifyEmailService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository
    ) {
        this.tokenRepository =
                Objects.requireNonNull(tokenRepository, "EmailVerificationTokenRepository cannot be null.");
        this.userRepository =
                Objects.requireNonNull(userRepository, "UserRepository cannot be null.");
    }

    @Transactional
    public void execute(String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Invalid or expired verification token.");
        }

        EmailVerificationToken token = tokenRepository
                .findByTokenHash(TokenHasher.sha256Hex(rawToken))
                .filter(EmailVerificationToken::isUsable)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token."));

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token."));

        tokenRepository.save(token.markUsed());
        userRepository.save(user.verifyEmail());
    }
}
