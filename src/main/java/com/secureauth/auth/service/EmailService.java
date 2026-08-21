package com.secureauth.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendPasswordResetEmail(String toEmail, String rawToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(fromAddress, "SecureAuth");
            helper.setTo(toEmail);
            helper.setSubject("Reset your SecureAuth password");
            helper.setText(buildHtmlContent(rawToken), true);

            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to send password reset email", e);
        }
    }

    private String buildHtmlContent(String rawToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + rawToken;

        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 24px; border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #2c3e50;">Password Reset Request</h2>
                    <p style="color: #333; font-size: 15px; line-height: 1.5;">
                        You requested to reset your SecureAuth password. Click the button below to choose a new password.
                        This link is valid for <strong>15 minutes</strong>.
                    </p>
                    <div style="text-align: center; margin: 28px 0;">
                        <a href="%s" style="background-color: #2c3e50; color: #ffffff; text-decoration: none; padding: 12px 28px; border-radius: 6px; font-size: 15px; font-weight: bold; display: inline-block;">
                            Reset My Password
                        </a>
                    </div>
                    <p style="color: #999; font-size: 12px; text-align: center;">
                        Or copy this link: <br>
                        <span style="word-break: break-all;">%s</span>
                    </p>
                    <div style="background-color: #f4f6f8; border-radius: 6px; padding: 16px; margin: 20px 0; text-align: center;">
                        <p style="color: #555; font-size: 12px; margin: 0 0 6px 0;">Your reset token:</p>
                        <code style="font-size: 15px; font-weight: bold; color: #2c3e50; word-break: break-all;">%s</code>
                    </div>
                    <p style="color: #777; font-size: 13px; line-height: 1.5;">
                        If you did not request this password reset, you can safely ignore this email —
                        your password will remain unchanged.
                    </p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 24px 0;">
                    <p style="color: #aaa; font-size: 12px;">SecureAuth — Secure Authentication Module</p>
                </div>
                """.formatted(resetLink, resetLink, rawToken);
    }
}