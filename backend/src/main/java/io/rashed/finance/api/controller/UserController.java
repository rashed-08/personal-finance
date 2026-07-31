package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.auth.AuthDtoMapper;
import io.rashed.finance.api.dto.auth.UserResponse;
import io.rashed.finance.application.user.GetUserService;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.security.AuthenticatedUser;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetUserService getUserService;

    public UserController(GetUserService getUserService) {
        this.getUserService = getUserService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        User user = getUserService.findById(UserId.of(principal.id()));

        return AuthDtoMapper.toResponse(user);
    }
}
