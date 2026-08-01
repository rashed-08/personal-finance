package io.rashed.finance.infrastructure.bootstrap;

import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a ready-to-use account on startup so a fresh database can be
 * logged into without registering first.
 *
 * Disabled unless {@code app.default-user.enabled} is true — it is only
 * switched on in the {@code local} profile. A known password must never
 * exist in a deployed environment.
 */
@Component
@ConditionalOnProperty(name = "app.default-user.enabled", havingValue = "true")
public class DefaultUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultUserInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String name;

    public DefaultUserInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.default-user.email}") String email,
            @Value("${app.default-user.password}") String password,
            @Value("${app.default-user.name}") String name
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (userRepository.existsByEmail(email)) {
            log.info("Default user {} already exists; leaving it untouched.", email);
            return;
        }

        // Pre-verified: the point is to skip the email round-trip entirely.
        User user = User.registerLocal(email, passwordEncoder.encode(password), name)
                .verifyEmail();

        userRepository.save(user);

        log.warn(
                """

                ================================================================
                 Created the default development user
                   email:    {}
                   password: {}
                 Set app.default-user.enabled=false (or DEFAULT_USER_ENABLED=false)
                 to stop creating it. Never enable this outside local development.
                ================================================================""",
                email, password
        );
    }
}
