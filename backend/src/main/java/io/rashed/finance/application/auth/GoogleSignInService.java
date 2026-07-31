package io.rashed.finance.application.auth;

import io.rashed.finance.common.exception.InvalidCredentialsException;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Signs a user in with a Google ID token, creating or linking the
 * account as needed:
 *
 * 1. Known Google subject → sign in.
 * 2. Existing user with the same (Google-verified) email → link the
 *    Google identity to that account.
 * 3. Otherwise → register a new user.
 */
@Service
public final class GoogleSignInService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserRepository userRepository;
    private final IssueTokensService issueTokensService;

    public GoogleSignInService(
            GoogleTokenVerifier googleTokenVerifier,
            UserRepository userRepository,
            IssueTokensService issueTokensService
    ) {
        this.googleTokenVerifier =
                Objects.requireNonNull(googleTokenVerifier, "GoogleTokenVerifier cannot be null.");
        this.userRepository =
                Objects.requireNonNull(userRepository, "UserRepository cannot be null.");
        this.issueTokensService =
                Objects.requireNonNull(issueTokensService, "IssueTokensService cannot be null.");
    }

    @Transactional
    public AuthResult execute(String idToken) {

        GoogleUserInfo info = googleTokenVerifier.verify(idToken)
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!info.emailVerified()) {
            // Never link or create accounts from an email Google has not
            // verified — it could belong to someone else.
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByProviderSubject(info.subject())
                .orElseGet(() -> linkOrRegister(info));

        return issueTokensService.execute(user);
    }

    private User linkOrRegister(GoogleUserInfo info) {

        Optional<User> byEmail = userRepository.findByEmail(info.email());

        if (byEmail.isPresent()) {
            return userRepository.save(byEmail.get().linkGoogle(info.subject()));
        }

        String name = (info.name() == null || info.name().isBlank())
                ? info.email()
                : info.name();

        return userRepository.save(
                User.registerWithGoogle(info.email(), name, info.subject(), true)
        );
    }
}
