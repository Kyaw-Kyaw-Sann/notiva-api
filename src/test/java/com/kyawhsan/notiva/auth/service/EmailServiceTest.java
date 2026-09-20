package com.kyawhsan.notiva.auth.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@notiva.example");
        ReflectionTestUtils.setField(emailService, "backendUrl", "https://api.notiva.example");
        ReflectionTestUtils.setField(emailService, "verificationExpirationHours", 24L);
        ReflectionTestUtils.setField(emailService, "passwordResetOtpExpirationMinutes", 10L);
    }

    @Test
    void sendsBrandedHtmlVerificationEmailWithPlainTextFallback() throws Exception {
        MimeMessage message = mimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendVerificationEmail("user@example.com", "Aung <Aung>", "token-123");

        String emailSource = sourceOf(sentMessage());

        assertTrue(emailSource.contains("Verify your Notiva email"));
        assertTrue(emailSource.contains("Welcome to Notiva"));
        assertTrue(emailSource.contains("background:#5B5CE2"));
        assertTrue(emailSource.contains("Aung &lt;Aung&gt;"));
        assertTrue(emailSource.contains("https://api.notiva.example/api/auth/verify-email?token=token-123"));
        assertTrue(emailSource.contains("This link expires in 24 hours"));
    }

    @Test
    void sendsBrandedPasswordResetOtpEmail() throws Exception {
        MimeMessage message = mimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(message);

        emailService.sendPasswordResetOtp("user@example.com", "Aung Aung", "123456");

        String emailSource = sourceOf(sentMessage());

        assertTrue(emailSource.contains("Your Notiva password reset code"));
        assertTrue(emailSource.contains("Your reset code"));
        assertTrue(emailSource.contains(">123456</p>"));
        assertTrue(emailSource.contains("This code expires in 10 minutes"));
        assertTrue(emailSource.contains("background:#F6F6FF"));
    }

    private MimeMessage sentMessage() {
        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        return captor.getValue();
    }

    private MimeMessage mimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    private String sourceOf(MimeMessage message) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        message.writeTo(output);
        return output.toString(StandardCharsets.UTF_8);
    }
}
