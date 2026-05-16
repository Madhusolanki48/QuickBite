package com.quickbite.authservice.controller;

import com.quickbite.authservice.dto.*;
import com.quickbite.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/auth", "/auth"})
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    @PostMapping("/verify-registration")
    public ResponseEntity<AuthResponse> verifyRegistration(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyRegistration(request));
    }

    @PostMapping("/resend-registration-otp")
    public ResponseEntity<MessageResponse> resendRegistrationOtp(
            @Valid @RequestBody ResendRegistrationOtpRequest request) {
        return ResponseEntity.ok(authService.resendRegistrationOtp(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        PasswordResetResponse response = authService.forgotPassword(request);
        HttpStatus status = response.success()
                ? HttpStatus.OK
                : "User not found".equalsIgnoreCase(response.message())
                ? HttpStatus.NOT_FOUND
                : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping({"/verify-otp", "/verify-reset-otp"})
    public ResponseEntity<PasswordResetResponse> verifyResetOtp(@Valid @RequestBody VerifyOtpRequest request) {
        PasswordResetResponse response = authService.verifyPasswordResetOtp(request);
        HttpStatus status = response.success()
                ? HttpStatus.OK
                : "User not found".equalsIgnoreCase(response.message())
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        PasswordResetResponse response = authService.resetPassword(request);
        HttpStatus status = response.success()
                ? HttpStatus.OK
                : "User not found".equalsIgnoreCase(response.message())
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.getCurrentUser(authentication));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateCurrentUser(authentication, request));
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidationResponse> validate(Authentication authentication) {
        return ResponseEntity.ok(authService.validate(authentication));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> notifications(Authentication authentication) {
        return ResponseEntity.ok(authService.listNotifications(authentication));
    }

    @PostMapping("/notifications")
    public ResponseEntity<NotificationResponse> createNotification(Authentication authentication,
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(authService.createNotification(authentication, request));
    }

    @PatchMapping("/notifications/{id}/read")
    public ResponseEntity<NotificationResponse> readNotification(Authentication authentication, @PathVariable Long id) {
        return ResponseEntity.ok(authService.markNotificationRead(authentication, id));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<AdminUserResponse>> users(Authentication authentication) {
        return ResponseEntity.ok(authService.listUsers(authentication));
    }

    @PatchMapping("/admin/users/{id}/role")
    public ResponseEntity<AdminUserResponse> changeRole(Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ResponseEntity.ok(authService.updateUserRole(authentication, id, request));
    }

    @PatchMapping("/admin/users/{id}/enabled")
    public ResponseEntity<AdminUserResponse> changeEnabled(Authentication authentication, @PathVariable Long id,
            @Valid @RequestBody UpdateEnabledRequest request) {
        return ResponseEntity.ok(authService.updateUserEnabled(authentication, id, request));
    }
}
