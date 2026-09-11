package io.rashed.finance.domain.users;

import java.util.Optional;

public interface GoogleOAuthTokenRepository {

    GoogleOAuthToken save(GoogleOAuthToken token);

    Optional<GoogleOAuthToken> findByUserAndScope(UserId userId, GoogleOAuthScope scope);

    /**
     * Any grant for the scope, regardless of user.
     *
     * The application is single-owner (no financial table is scoped to a
     * user), so a scheduled backup has no user context to work from and
     * uses whichever Drive grant exists.
     */
    Optional<GoogleOAuthToken> findAnyByScope(GoogleOAuthScope scope);

    void deleteByUserAndScope(UserId userId, GoogleOAuthScope scope);
}
