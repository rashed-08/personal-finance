package io.rashed.finance.api.controller;

import io.rashed.finance.api.dto.auth.AuthDtoMapper;
import io.rashed.finance.api.dto.auth.AuthResponse;
import io.rashed.finance.api.dto.auth.GoogleSignInRequest;
import io.rashed.finance.api.dto.auth.LoginRequest;
import io.rashed.finance.api.dto.auth.RegisterRequest;
import io.rashed.finance.application.auth.AuthResult;
import io.rashed.finance.application.auth.GoogleSignInService;
import io.rashed.finance.application.auth.LoginService;
import io.rashed.finance.application.auth.LogoutService;
import io.rashed.finance.application.auth.RefreshTokenService;
import io.rashed.finance.application.auth.RegisterUserService;
import jakarta.validation.Valid;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Scoped to /api/auth so the browser only sends the refresh token
     * to the endpoints that actually need it.
     */
    static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private final RegisterUserService registerUserService;
    private final LoginService loginService;
    private final GoogleSignInService googleSignInService;
    private final RefreshTokenService refreshTokenService;
    private final LogoutService logoutService;
    private final boolean cookieSecure;

    public AuthController(
            RegisterUserService registerUserService,
            LoginService loginService,
            GoogleSignInService googleSignInService,
            RefreshTokenService refreshTokenService,
            LogoutService logoutService,
            @Value("${app.security.cookie-secure:false}") boolean cookieSecure
    ) {
        this.registerUserService = registerUserService;
        this.loginService = loginService;
        this.googleSignInService = googleSignInService;
        this.refreshTokenService = refreshTokenService;
        this.logoutService = logoutService;
        this.cookieSecure = cookieSecure;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        AuthResult result = registerUserService.execute(
                AuthDtoMapper.toCommand(request)
        );

        return withRefreshCookie(HttpStatus.CREATED, result);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthResult result = loginService.execute(
                AuthDtoMapper.toCommand(request)
        );

        return withRefreshCookie(HttpStatus.OK, result);
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleSignIn(
            @Valid @RequestBody GoogleSignInRequest request
    ) {

        AuthResult result = googleSignInService.execute(request.idToken());

        return withRefreshCookie(HttpStatus.OK, result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {

        AuthResult result = refreshTokenService.execute(refreshToken);

        return withRefreshCookie(HttpStatus.OK, result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken
    ) {

        logoutService.execute(refreshToken);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expiredRefreshCookie().toString())
                .build();
    }

    private ResponseEntity<AuthResponse> withRefreshCookie(HttpStatus status, AuthResult result) {

        ResponseCookie cookie = refreshCookie(
                result.refreshToken(),
                result.refreshTokenTtl()
        );

        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(AuthDtoMapper.toResponse(result));
    }

    private ResponseCookie refreshCookie(String value, Duration maxAge) {

        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/auth")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
    }

    private ResponseCookie expiredRefreshCookie() {

        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/auth")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
    }
}
