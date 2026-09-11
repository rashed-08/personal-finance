package io.rashed.finance.api.dto.backup;

import jakarta.validation.constraints.NotBlank;

/**
 * Authorizes a restore.
 *
 * @param confirmation must be exactly
 *        {@code RestoreBackupService.CONFIRMATION}. A typed phrase rather
 *        than a boolean so an empty or default-constructed body can never
 *        authorize replacing the ledger.
 */
public record RestoreBackupRequest(

        @NotBlank(message = "Confirmation is required to restore a backup.")
        String confirmation

) {
}
