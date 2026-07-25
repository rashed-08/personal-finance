package io.rashed.finance.api.dto.migration;

import jakarta.validation.constraints.NotBlank;

public record ImportGoogleKeepRequest(

        @NotBlank(message = "Google Keep export content is required.")
        String content

) {
}
