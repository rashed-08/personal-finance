package io.rashed.finance.common.exception;

/**
 * Thrown when email/password authentication fails.
 *
 * Mapped to HTTP 401. The message is deliberately generic so the
 * response does not reveal whether the email exists.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
