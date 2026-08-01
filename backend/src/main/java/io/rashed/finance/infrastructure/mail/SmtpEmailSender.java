package io.rashed.finance.infrastructure.mail;

import io.rashed.finance.application.auth.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Sends plain-text email through Spring Mail (SMTP).
 *
 * Sending is best-effort: failures are logged, never propagated, so a
 * mail outage cannot break registration or the forgot-password flow
 * (which must not leak whether the email exists anyway).
 *
 * When {@code app.mail.enabled} is false nothing is sent and the message
 * is written to the log instead, so local development can follow
 * verification and reset links without running an SMTP server.
 */
@Component
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;
    private final boolean enabled;

    public SmtpEmailSender(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:noreply@personal-finance.local}") String from,
            @Value("${app.mail.enabled:true}") boolean enabled
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
        this.enabled = enabled;
    }

    @Override
    public void send(String to, String subject, String body) {

        if (!enabled) {
            logInstead(to, subject, body);
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            log.warn(
                    "Mail is enabled but no mail server is configured (spring.mail.host). "
                            + "Dropping email to {} [{}]. Set app.mail.enabled=false to log messages instead.",
                    to, subject
            );
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

        } catch (Exception ex) {
            // No stack trace: delivery is best-effort and the root message
            // ("Connection refused") is the only actionable part. A full trace
            // here is ~130 lines of framework noise on every registration.
            log.warn(
                    "Could not send email to {} [{}]: {}. "
                            + "Start the Mailpit service from infra/compose.yaml, or set app.mail.enabled=false "
                            + "to log messages instead of sending them.",
                    to, subject, rootMessage(ex)
            );
        }
    }

    /**
     * Writes the whole message, one-time links included, to the log.
     *
     * That is the point in this mode — the developer copies the link from
     * the console — but it does mean tokens land in the log, so disabled
     * mail is for local development only, never production.
     */
    private void logInstead(String to, String subject, String body) {

        log.info(
                """
                [mail disabled] would send to {}
                  Subject: {}
                {}""",
                to, subject, body.strip().indent(2)
        );
    }

    private static String rootMessage(Throwable ex) {

        Throwable root = ex;

        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        return root.getMessage();
    }
}
