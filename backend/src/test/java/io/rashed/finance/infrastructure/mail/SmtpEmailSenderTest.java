package io.rashed.finance.infrastructure.mail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SmtpEmailSenderTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);

    @SuppressWarnings("unchecked")
    private ObjectProvider<JavaMailSender> providerOf(JavaMailSender sender) {

        ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(sender);

        return provider;
    }

    private SmtpEmailSender sender(JavaMailSender mail, boolean enabled) {
        return new SmtpEmailSender(providerOf(mail), "noreply@test.local", enabled);
    }

    @Test
    void send_deliversWhenEnabled() {

        sender(mailSender, true).send("to@test.local", "Subject", "Body");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_setsConfiguredFromAddress() {

        sender(mailSender, true).send("to@test.local", "Subject", "Body");

        var captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals("noreply@test.local", captor.getValue().getFrom());
        assertEquals("Subject", captor.getValue().getSubject());
    }

    @Test
    void send_doesNotTouchMailServerWhenDisabled() {

        sender(mailSender, false).send("to@test.local", "Subject", "Body with /verify-email?token=abc");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_swallowsDeliveryFailure() {

        doThrow(new MailSendException("Connection refused"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Registration and forgot-password must not fail because mail is down.
        assertDoesNotThrow(() ->
                sender(mailSender, true).send("to@test.local", "Subject", "Body"));
    }

    @Test
    void send_toleratesMissingMailSenderBean() {

        SmtpEmailSender withoutBean =
                new SmtpEmailSender(providerOf(null), "noreply@test.local", true);

        assertDoesNotThrow(() -> withoutBean.send("to@test.local", "Subject", "Body"));
    }
}
