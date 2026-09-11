package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.domain.users.GoogleOAuthToken;
import io.rashed.finance.domain.users.GoogleOAuthTokenId;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.persistence.entity.GoogleOAuthTokenEntity;

public final class GoogleOAuthTokenEntityMapper {

    private GoogleOAuthTokenEntityMapper() {
    }

    public static GoogleOAuthTokenEntity toEntity(GoogleOAuthToken token) {

        if (token == null) {
            return null;
        }

        return new GoogleOAuthTokenEntity(
                token.getId().getValue(),
                token.getUserId().getValue(),
                token.getScope(),
                token.getEncryptedRefreshToken(),
                token.getGrantedScopes(),
                token.getAccountEmail(),
                token.getConnectedAt(),
                token.getCreatedAt(),
                token.getUpdatedAt()
        );
    }

    public static GoogleOAuthToken toDomain(GoogleOAuthTokenEntity entity) {

        if (entity == null) {
            return null;
        }

        return new GoogleOAuthToken(
                GoogleOAuthTokenId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getScope(),
                entity.getRefreshToken(),
                entity.getGrantedScopes(),
                entity.getAccountEmail(),
                entity.getConnectedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
