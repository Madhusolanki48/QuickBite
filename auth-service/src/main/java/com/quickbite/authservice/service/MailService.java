package com.quickbite.authservice.service;

import com.quickbite.authservice.model.AppUser;
import com.quickbite.authservice.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:${spring.mail.username:}}")
    private String from;

    public void sendRegistrationOtp(AppUser user, String otp) {
        SimpleMailMessage message = baseMessage(user.getEmail(), "Verify your QuickBite account");
        message.setText("""
                Hi %s,

                Your QuickBite verification code is: %s

                This code expires in 10 minutes.
                If you did not create this account, you can safely ignore this email.
                """.formatted(user.getFirstName(), otp));
        mailSender.send(message);
    }

    public void sendWelcomeEmail(AppUser user) {
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
    }

    public void sendPasswordResetOtp(AppUser user, String otp) {
        SimpleMailMessage message = baseMessage(user.getEmail(), "QuickBite password reset code");
        message.setText("""
                Hi %s,

                Your password reset code is: %s

                This code expires in 10 minutes.
                If you did not request a password reset, please ignore this email.
                """.formatted(user.getFirstName(), otp));
        mailSender.send(message);
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
}
