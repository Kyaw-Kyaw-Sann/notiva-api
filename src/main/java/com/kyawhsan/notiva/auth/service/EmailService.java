package com.kyawhsan.notiva.auth.service;

import com.kyawhsan.notiva.common.exception.EmailSendingException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Value("${app.auth.email-verification-expiration-hours}")
    private long verificationExpirationHours;

    @Value("${app.auth.password-reset-otp-expiration-minutes}")
    private long passwordResetOtpExpirationMinutes;

    public void sendVerificationEmail(
            String recipientEmail,
            String displayName,
            String verificationToken) {
        String verificationLink = UriComponentsBuilder.fromUriString(backendUrl)
                .path("/api/auth/verify-email")
                .queryParam("token", verificationToken)
                .build()
                .encode()
                .toUriString();

        String plainText = """
                Hello %s,

                Welcome to Notiva. Verify your email address to finish setting up your account:

                %s

                This link expires in %d hours. If you did not create a Notiva account, you can safely ignore this email.

                Notiva
                """.formatted(displayName, verificationLink, verificationExpirationHours);

        sendEmail(recipientEmail, "Verify your Notiva email", plainText,
                verificationEmailHtml(displayName, verificationLink));
    }

    public void sendPasswordResetOtp(
            String recipientEmail,
            String displayName,
            String otp) {
        String plainText = """
                Hello %s,

                Use this code to reset your Notiva password:

                %s

                This code expires in %d minutes. Do not share it with anyone.

                If you did not request a password reset, you can safely ignore this email.

                Notiva
                """.formatted(displayName, otp, passwordResetOtpExpirationMinutes);

        sendEmail(recipientEmail, "Your Notiva password reset code", plainText,
                passwordResetOtpHtml(displayName, otp));
    }

    private void sendEmail(
            String recipientEmail,
            String subject,
            String plainText,
            String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true,
                    StandardCharsets.UTF_8.name());

            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(plainText, html);

            mailSender.send(message);
        } catch (MessagingException | RuntimeException exception) {
            throw new EmailSendingException("Unable to send email", exception);
        }
    }

    private String verificationEmailHtml(
            String displayName,
            String verificationLink) {
        String safeName = HtmlUtils.htmlEscape(displayName);
        String safeLink = HtmlUtils.htmlEscape(verificationLink);

        String content = """
                <p style="margin:0 0 16px;color:#344054;font-size:16px;line-height:24px;">Hello %s,</p>
                <h1 style="margin:0 0 16px;color:#111827;font-size:26px;line-height:34px;">Welcome to Notiva</h1>
                <p style="margin:0 0 24px;color:#475467;font-size:16px;line-height:24px;">Verify your email address to finish setting up your private workspace.</p>
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" style="margin:0 0 24px;"><tr><td style="border-radius:10px;background:#5B5CE2;"><a href="%s" style="display:inline-block;padding:13px 22px;color:#ffffff;font-size:16px;font-weight:700;line-height:20px;text-decoration:none;">Verify email</a></td></tr></table>
                <p style="margin:0 0 12px;color:#667085;font-size:14px;line-height:21px;">This link expires in %d hours.</p>
                <p style="margin:0;color:#667085;font-size:14px;line-height:21px;">If you did not create a Notiva account, you can safely ignore this email.</p>
                <p style="margin:24px 0 0;color:#98A2B3;font-size:12px;line-height:18px;word-break:break-all;">Button not working? Copy this link: %s</p>
                """.formatted(safeName, safeLink, verificationExpirationHours, safeLink);

        return emailLayout("Verify your email", content);
    }

    private String passwordResetOtpHtml(
            String displayName,
            String otp) {
        String safeName = HtmlUtils.htmlEscape(displayName);
        String safeOtp = HtmlUtils.htmlEscape(otp);

        String content = """
                <p style="margin:0 0 16px;color:#344054;font-size:16px;line-height:24px;">Hello %s,</p>
                <h1 style="margin:0 0 16px;color:#111827;font-size:26px;line-height:34px;">Reset your password</h1>
                <p style="margin:0 0 24px;color:#475467;font-size:16px;line-height:24px;">Use this one-time code to reset your Notiva password.</p>
                <div style="margin:0 0 24px;padding:20px;border:1px solid #DDDDFB;border-radius:12px;background:#F6F6FF;text-align:center;"><p style="margin:0 0 8px;color:#667085;font-size:13px;font-weight:700;letter-spacing:0.08em;text-transform:uppercase;">Your reset code</p><p style="margin:0;color:#4F46E5;font-family:Arial,sans-serif;font-size:32px;font-weight:700;letter-spacing:0.20em;line-height:40px;">%s</p></div>
                <p style="margin:0 0 12px;color:#667085;font-size:14px;line-height:21px;">This code expires in %d minutes. Do not share it with anyone.</p>
                <p style="margin:0;color:#667085;font-size:14px;line-height:21px;">If you did not request a password reset, you can safely ignore this email.</p>
                """.formatted(safeName, safeOtp, passwordResetOtpExpirationMinutes);

        return emailLayout("Password reset code", content);
    }

    private String emailLayout(
            String preheader,
            String content) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>%s</title>
                </head>
                <body style="margin:0;padding:0;background:#F6F6FF;font-family:Arial,Helvetica,sans-serif;">
                  <div style="display:none;max-height:0;overflow:hidden;opacity:0;color:transparent;">%s</div>
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#F6F6FF;"><tr><td align="center" style="padding:32px 16px;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="max-width:600px;background:#FFFFFF;border:1px solid #E4E7EC;border-radius:16px;overflow:hidden;">
                      <tr><td style="padding:26px 32px;border-bottom:1px solid #E4E7EC;"><span style="display:inline-block;width:28px;height:28px;border-radius:8px;background:#5B5CE2;color:#FFFFFF;font-size:20px;font-weight:700;line-height:28px;text-align:center;vertical-align:middle;">N</span><span style="margin-left:10px;color:#111827;font-size:22px;font-weight:700;line-height:28px;vertical-align:middle;">Notiva</span></td></tr>
                      <tr><td style="padding:32px;">%s</td></tr>
                      <tr><td style="padding:20px 32px;border-top:1px solid #E4E7EC;background:#FCFCFD;"><p style="margin:0;color:#98A2B3;font-size:12px;line-height:18px;">Notiva · Private notes, thoughtfully organized</p></td></tr>
                    </table>
                  </td></tr></table>
                </body>
                </html>
                """.formatted(preheader, preheader, content);
    }
}
