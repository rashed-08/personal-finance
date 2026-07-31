package io.rashed.finance.application.auth;

/**
 * Outbound email port. Implemented in the infrastructure layer
 * (SMTP via Spring Mail; Mailpit in local development).
 */
public interface EmailSender {

    void send(String to, String subject, String body);
}
