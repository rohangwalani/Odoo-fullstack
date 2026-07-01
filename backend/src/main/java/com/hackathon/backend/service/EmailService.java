package com.hackathon.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for all outbound emails.
 * Currently uses Mailtrap for safe testing (no real emails sent).
 * To switch to production: update application.properties SMTP settings.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a welcome email to a newly registered user.
     * Visible in Mailtrap inbox during development.
     */
    public void sendWelcomeEmail(String to, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Welcome to HackBase! 🚀");
        message.setText(
                "Hi " + username + ",\n\n" +
                "Welcome to HackBase! Your account has been successfully created.\n\n" +
                "Account Details:\n" +
                "  Username : " + username + "\n" +
                "  Email    : " + to + "\n\n" +
                "You can now log in and start using the platform.\n\n" +
                "Happy hacking! 🎉\n" +
                "— The HackBase Team"
        );
        mailSender.send(message);
    }

    /**
     * Sends a password reset link email.
     */
    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("Password Reset Request — HackBase");
        message.setText(
                "You requested a password reset.\n\n" +
                "Click the link below to reset your password:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in 15 minutes.\n" +
                "If you didn't request this, you can safely ignore this email.\n\n" +
                "— The HackBase Team"
        );
        mailSender.send(message);
    }
}
