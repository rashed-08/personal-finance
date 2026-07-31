package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.domain.users.EmailVerificationToken;
import io.rashed.finance.domain.users.EmailVerificationTokenId;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.persistence.entity.EmailVerificationTokenEntity;

public final class EmailVerificationTokenEntityMapper {

    private EmailVerificationTokenEntityMapper() {
    }

    public static EmailVerificationTokenEntity toEntity(EmailVerificationToken token) {

        if (token == null) {
            return null;
        }

        return new EmailVerificationTokenEntity(
                token.getId().getValue(),
                token.getUserId().getValue(),
                token.getTokenHash(),
                token.getExpiresAt(),
                token.getUsedAt(),
                token.getCreatedAt()
        );
    }

    public static EmailVerificationToken toDomain(EmailVerificationTokenEntity entity) {

        if (entity == null) {
            return null;
        }

        return new EmailVerificationToken(
                EmailVerificationTokenId.of(entity.getId()),
                UserId.of(entity.getUserId()),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }
}
