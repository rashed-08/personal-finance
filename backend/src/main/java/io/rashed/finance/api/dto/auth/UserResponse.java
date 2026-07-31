package io.rashed.finance.api.dto.auth;

import io.rashed.finance.common.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(

        UUID id,

        String email,

        String name,

        UserRole role,

        boolean emailVerified,

        LocalDateTime createdAt

) {
}
