package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.auth.AuthDtoMapper;
import io.rashed.finance.api.dto.auth.ChangePasswordRequest;
import io.rashed.finance.api.dto.auth.UserResponse;
import io.rashed.finance.application.auth.ChangePasswordService;
import io.rashed.finance.application.user.GetUserService;
import io.rashed.finance.domain.users.User;
import io.rashed.finance.domain.users.UserId;
import io.rashed.finance.infrastructure.security.AuthenticatedUser;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final GetUserService getUserService;
    private final ChangePasswordService changePasswordService;

    public UserController(
            GetUserService getUserService,
            ChangePasswordService changePasswordService
    ) {
        this.getUserService = getUserService;
        this.changePasswordService = changePasswordService;
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {

        User user = getUserService.findById(UserId.of(principal.id()));

        return AuthDtoMapper.toResponse(user);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        changePasswordService.execute(
                UserId.of(principal.id()),
                request.currentPassword(),
                request.newPassword()
        );
    }
}
