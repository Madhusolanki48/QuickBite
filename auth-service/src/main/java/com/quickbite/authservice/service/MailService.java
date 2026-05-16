package com.quickbite.authservice.service;

import com.quickbite.authservice.model.AppUser;
import com.quickbite.authservice.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String from;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @PostConstruct
    void logMailConfiguration() {
        if (isBlank(username) || isBlank(password)) {
            log.warn("Gmail SMTP is not fully configured. OTP, reset, and welcome emails will not be sent until GMAIL_USERNAME and GMAIL_APP_PASSWORD are set.");
        }
    }

    @Async
    public void sendRegistrationOtp(AppUser user, String otp) {
        try {
            SimpleMailMessage message = baseMessage(user.getEmail(), "Verify your QuickBite account");
            message.setText("""
                    Hi %s,

                    Your QuickBite verification code is: %s

                    This code expires in 10 minutes.
                    If you did not create this account, you can safely ignore this email.
                    """.formatted(user.getFirstName(), otp));
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send registration OTP email to {}", user.getEmail(), ex);
        }
    }

    @Async
    public void sendWelcomeEmail(AppUser user) {
        try {
            SimpleMailMessage message = baseMessage(user.getEmail(), "Welcome to QuickBite");
            String body = user.getRole() == Role.RESTAURANT_OWNER
                    ? "Your account is verified and waiting for admin approval before you can log in."
                    : "Your account is now active and you can log in to QuickBite anytime.";
            message.setText("""
                    Hi %s,

                    Welcome to QuickBite.
                    %s

                    We are glad to have you with us.
                    """.formatted(user.getFirstName(), body));
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send welcome email to {}", user.getEmail(), ex);
        }
    }

    @Async
    public void sendPasswordResetOtp(AppUser user, String otp) {
        try {
            SimpleMailMessage message = baseMessage(user.getEmail(), "QuickBite password reset code");
            message.setText("""
                    Hi %s,

                    Your password reset code is: %s

                    This code expires in 10 minutes.
                    If you did not request a password reset, please ignore this email.
                    """.formatted(user.getFirstName(), otp));
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send password reset OTP email to {}", user.getEmail(), ex);
        }
    }

    private SimpleMailMessage baseMessage(String to, String subject) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        return message;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
