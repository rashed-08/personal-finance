package io.rashed.finance.api.dto.auth;

import io.rashed.finance.application.auth.AuthResult;
import io.rashed.finance.application.auth.LoginCommand;
import io.rashed.finance.application.auth.RegisterUserCommand;
import io.rashed.finance.domain.users.User;

public final class AuthDtoMapper {

    private AuthDtoMapper() {
    }

    public static RegisterUserCommand toCommand(RegisterRequest request) {

        if (request == null) {
            return null;
        }

        return new RegisterUserCommand(
                request.email(),
                request.password(),
                request.name()
        );
    }

    public static LoginCommand toCommand(LoginRequest request) {

        if (request == null) {
            return null;
        }

        return new LoginCommand(
                request.email(),
                request.password()
        );
    }

    public static AuthResponse toResponse(AuthResult result) {

        if (result == null) {
            return null;
        }

        return new AuthResponse(
                result.accessToken(),
                "Bearer",
                result.accessTokenExpiresIn(),
                toResponse(result.user())
        );
    }

    public static UserResponse toResponse(User user) {

        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId().getValue(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isEmailVerified(),
                user.getCreatedAt()
        );
    }
}
