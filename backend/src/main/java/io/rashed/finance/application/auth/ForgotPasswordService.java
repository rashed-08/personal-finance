package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.PasswordResetToken;
import io.rashed.finance.domain.users.PasswordResetTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Starts the forgot-password flow.
 *
 * Always completes silently — whether the email exists, or belongs to
 * a Google-only account, must not be observable (user enumeration).
 */
@Service
public final class ForgotPasswordService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final Duration tokenTtl;
    private final String frontendBaseUrl;

    public ForgotPasswordService(
            PasswordResetTokenRepository tokenRepository,
            UserRepository userRepository,
            EmailSender emailSender,
            @Value("${app.security.tokens.password-reset-ttl:1h}") Duration tokenTtl,
            @Value("${app.frontend-base-url:http://localhost:5173}") String frontendBaseUrl
    ) {
        this.tokenRepository =
                Objects.requireNonNull(tokenRepository, "PasswordResetTokenRepository cannot be null.");
        this.userRepository =
                Objects.requireNonNull(userRepository, "UserRepository cannot be null.");
        this.emailSender =
                Objects.requireNonNull(emailSender, "EmailSender cannot be null.");
        this.tokenTtl = Objects.requireNonNull(tokenTtl);
        this.frontendBaseUrl = Objects.requireNonNull(frontendBaseUrl);
    }

    public void execute(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email)
                .filter(User::hasLocalPassword)
                .ifPresent(this::sendResetEmail);
    }

    private void sendResetEmail(User user) {

        String rawToken = OpaqueTokenGenerator.generate();

        tokenRepository.save(PasswordResetToken.issue(
                user.getId(),
                TokenHasher.sha256Hex(rawToken),
                LocalDateTime.now().plus(tokenTtl)
        ));

        String link = frontendBaseUrl + "/reset-password?token=" + rawToken;

        emailSender.send(
                user.getEmail(),
                "Reset your password — Personal Finance",
                """
                Hi %s,

                We received a request to reset your password. Open the link below to choose a new one:

                %s

                The link expires in %d minutes and can be used once. If you did not request this, ignore this email — your password is unchanged.
                """.formatted(user.getName(), link, tokenTtl.toMinutes())
        );
    }
}
