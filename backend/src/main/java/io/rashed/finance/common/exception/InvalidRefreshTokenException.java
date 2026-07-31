package io.rashed.finance.common.exception;

/**
 * Thrown when a refresh token is missing, unknown, expired or revoked.
 *
 * Mapped to HTTP 401 so the client knows to re-authenticate.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
