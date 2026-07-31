package io.rashed.finance.application.auth;

import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public final class RegisterUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IssueTokensService issueTokensService;

    public RegisterUserService(
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

    public AuthResult execute(RegisterUserCommand command) {

        Objects.requireNonNull(command, "RegisterUserCommand cannot be null.");

        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        User user = User.registerLocal(
                command.email(),
                passwordEncoder.encode(command.password()),
                command.name()
        );

        User saved = userRepository.save(user);

        return issueTokensService.execute(saved);
    }
}
