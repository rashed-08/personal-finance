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
 */
@Component
public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;

    public SmtpEmailSender(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${app.mail.from:noreply@personal-finance.local}") String from
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String body) {

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            log.warn("No mail server configured (spring.mail.host); dropping email to {} [{}]", to, subject);
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
            log.error("Failed to send email to {} [{}]", to, subject, ex);
        }
    }
}
