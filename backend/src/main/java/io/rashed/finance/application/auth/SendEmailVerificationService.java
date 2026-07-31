package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.EmailVerificationToken;
import io.rashed.finance.domain.users.EmailVerificationTokenRepository;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Issues an email-verification token and emails the verification link.
 */
@Service
public final class SendEmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final Duration tokenTtl;
    private final String frontendBaseUrl;

    public SendEmailVerificationService(
            EmailVerificationTokenRepository tokenRepository,
            UserRepository userRepository,
            EmailSender emailSender,
            @Value("${app.security.tokens.email-verification-ttl:24h}") Duration tokenTtl,
            @Value("${app.frontend-base-url:http://localhost:5173}") String frontendBaseUrl
    ) {
        this.tokenRepository =
                Objects.requireNonNull(tokenRepository, "EmailVerificationTokenRepository cannot be null.");
        this.userRepository =
                Objects.requireNonNull(userRepository, "UserRepository cannot be null.");
        this.emailSender =
                Objects.requireNonNull(emailSender, "EmailSender cannot be null.");
        this.tokenTtl = Objects.requireNonNull(tokenTtl);
        this.frontendBaseUrl = Objects.requireNonNull(frontendBaseUrl);
    }

    public void sendFor(User user) {

        Objects.requireNonNull(user, "User cannot be null.");

        if (user.isEmailVerified()) {
            return;
        }

        String rawToken = OpaqueTokenGenerator.generate();

        tokenRepository.save(EmailVerificationToken.issue(
                user.getId(),
                TokenHasher.sha256Hex(rawToken),
                LocalDateTime.now().plus(tokenTtl)
        ));

        String link = frontendBaseUrl + "/verify-email?token=" + rawToken;

        emailSender.send(
                user.getEmail(),
                "Verify your email — Personal Finance",
                """
                Hi %s,

                Please confirm your email address by opening the link below:

                %s

                The link expires in %d hours. If you did not create this account, ignore this email.
                """.formatted(user.getName(), link, tokenTtl.toHours())
        );
    }

    /**
     * Resend flow. Always succeeds silently — an unknown or already
     * verified email must not be distinguishable from a successful send.
     */
    public void resend(String email) {

        if (email == null || email.isBlank()) {
            return;
        }

        userRepository.findByEmail(email)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendFor);
    }
}
