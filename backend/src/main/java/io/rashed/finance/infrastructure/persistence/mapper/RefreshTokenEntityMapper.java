package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.domain.users.RefreshToken;
import io.rashed.finance.domain.users.RefreshTokenId;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.persistence.entity.RefreshTokenEntity;

public final class RefreshTokenEntityMapper {

    private RefreshTokenEntityMapper() {
    }

    public static RefreshTokenEntity toEntity(RefreshToken refreshToken) {

        if (refreshToken == null) {
            return null;
        }

        return new RefreshTokenEntity(
                refreshToken.getId().getValue(),
                refreshToken.getUserId().getValue(),
                refreshToken.getTokenHash(),
                refreshToken.getExpiresAt(),
                refreshToken.getRevokedAt(),
                refreshToken.getCreatedAt()
        );
    }

    public static RefreshToken toDomain(RefreshTokenEntity entity) {

        if (entity == null) {
            return null;
        }

        return new RefreshToken(
                RefreshTokenId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getCreatedAt()
        );
    }
}
