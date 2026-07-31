package io.rashed.finance.infrastructure.persistence.mapper;

import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.persistence.entity.UserEntity;

public final class UserEntityMapper {

    private UserEntityMapper() {
    }

    public static UserEntity toEntity(User user) {

        if (user == null) {
            return null;
        }

        return new UserEntity(
                user.getId().getValue(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getName(),
                user.getRole(),
                user.getProvider(),
                user.getProviderSubject(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static User toDomain(UserEntity entity) {

        if (entity == null) {
            return null;
        }

        return new User(
                UserId.of(entity.getId()),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getName(),
                entity.getRole(),
                entity.getProvider(),
                entity.getProviderSubject(),
                entity.isEmailVerified(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
