package io.rashed.finance.application.user;

import io.rashed.finance.common.exception.ResourceNotFoundException;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.domain.users.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public final class GetUserService {

    private final UserRepository userRepository;

    public GetUserService(UserRepository userRepository) {
        this.userRepository =
                Objects.requireNonNull(userRepository, "UserRepository cannot be null.");
    }

    public User findById(UserId id) {

        Objects.requireNonNull(id, "UserId cannot be null.");

        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + id.asString()
                ));
    }
}
