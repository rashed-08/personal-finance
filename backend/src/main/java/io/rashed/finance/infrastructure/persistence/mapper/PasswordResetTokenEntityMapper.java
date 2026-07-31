package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.domain.users.PasswordResetToken;
import io.rashed.finance.domain.users.PasswordResetTokenId;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.persistence.entity.PasswordResetTokenEntity;

public final class PasswordResetTokenEntityMapper {

    private PasswordResetTokenEntityMapper() {
    }

    public static PasswordResetTokenEntity toEntity(PasswordResetToken token) {

        if (token == null) {
            return null;
        }

        return new PasswordResetTokenEntity(
                token.getId().getValue(),
                token.getUserId().getValue(),
                token.getTokenHash(),
                token.getExpiresAt(),
                token.getUsedAt(),
                token.getCreatedAt()
        );
    }

    public static PasswordResetToken toDomain(PasswordResetTokenEntity entity) {

        if (entity == null) {
            return null;
        }

        return new PasswordResetToken(
                PasswordResetTokenId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }
}
