package io.rashed.finance.application.auth;

import io.rashed.finance.common.exception.InvalidCredentialsException;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public final class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IssueTokensService issueTokensService;

    public LoginService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            IssueTokensService issueTokensService
    ) {
        this.userRepository =
                Objects.requireNonNull(userRepository, "UserRepository cannot be null.");
        this.passwordEncoder =
                Objects.requireNonNull(passwordEncoder, "PasswordEncoder cannot be null.");
        this.issueTokensService =
                Objects.requireNonNull(issueTokensService, "IssueTokensService cannot be null.");
    }

    public AuthResult execute(LoginCommand command) {

        Objects.requireNonNull(command, "LoginCommand cannot be null.");

        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.hasLocalPassword()) {
            // Google-only account: no local password to check.
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return issueTokensService.execute(user);
    }
}
