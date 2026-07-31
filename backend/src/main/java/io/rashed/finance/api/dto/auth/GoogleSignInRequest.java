package io.rashed.finance.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInRequest(

        @NotBlank(message = "Google ID token is required.")
        String idToken

) {
}
