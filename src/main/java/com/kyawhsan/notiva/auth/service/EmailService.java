package com.kyawhsan.notiva.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.kyawhsan.notiva.common.exception.EmailSendingException;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.backend-url}")
    private String backendUrl;

    public void sendVerificationEmail(
            String recipientEmail,
            String displayName,
            String verificationToken) {
        String verificationLink = backendUrl + "/api/auth/verify-email?token=" + verificationToken;

        String message = """
                Hello %s,

                Thank you for registering with Notiva.

                Verify your email address using the link below:

                %s

                This link will expire soon.

                If you did not create this account, you can ignore this email.

                Notiva Team
                """.formatted(displayName, verificationLink);

        sendEmail(recipientEmail, "Verify your Notiva email", message);
    }

    public void sendPasswordResetOtp(
            String recipientEmail,
            String displayName,
            String otp) {
        String message = """
                Hello %s,

                Your Notiva password reset OTP is:

                %s

                This OTP will expire soon.

                Do not share this code with anyone.

                If you did not request a password reset, you can ignore this email.

                Notiva Team
                """.formatted(displayName, otp);

        sendEmail(recipientEmail, "Your Notiva password reset OTP", message);
    }

    private void sendEmail(
            String recipientEmail,
            String subject,
            String messageBody) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(messageBody);

            mailSender.send(message);
        } catch (Exception exception) {
            throw new EmailSendingException("Unable to send email", exception);
        }
    }
}