package io.rashed.finance.domain.users;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
