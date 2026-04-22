package com.quickbite.authservice.service;

import com.quickbite.authservice.dto.*;
import com.quickbite.authservice.exception.*;
import com.quickbite.authservice.model.AppUser;
import com.quickbite.authservice.model.ApprovalStatus;
import com.quickbite.authservice.model.Notification;
import com.quickbite.authservice.model.Role;
import com.quickbite.authservice.repository.NotificationRepository;
import com.quickbite.authservice.repository.UserRepository;
import com.quickbite.authservice.security.GoogleTokenVerifier;
import com.quickbite.authservice.messaging.NotificationEvent;
import com.quickbite.authservice.messaging.NotificationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationEventPublisher notificationEventPublisher;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) throw new ResourceAlreadyExistsException("Email is already registered");
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) throw new ResourceAlreadyExistsException("Phone number is already registered");
        Role role = request.role() == null ? Role.CUSTOMER : request.role();
        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Admin accounts cannot be registered from the form");
        }
        if (role == Role.RESTAURANT_OWNER && (request.restaurantId() == null || request.restaurantId().isBlank())) {
            throw new IllegalArgumentException("Restaurant selection is required for owner accounts");
        }
        AppUser user = AppUser.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email().toLowerCase())
                .phoneNumber(request.phoneNumber())
                .restaurantId(request.restaurantId() != null ? request.restaurantId().trim() : null)
                .password(passwordEncoder.encode(request.password()))
                .role(role)
                .approvalStatus(role == Role.RESTAURANT_OWNER ? ApprovalStatus.PENDING : ApprovalStatus.APPROVED)
                .enabled(role != Role.RESTAURANT_OWNER)
                .build();
        AppUser saved = userRepository.saveAndFlush(user);
        return new AuthResponse(jwtService.generateToken(saved), "Bearer", jwtService.getExpirationMs(), toResponse(saved));
    }
    public AuthResponse login(LoginRequest request) {
        String email = request.email().toLowerCase();
        AppUser user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Account is pending admin approval");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        return new AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationMs(), toResponse(user));
    }

    public AuthResponse googleLogin(GoogleLoginRequest request) {
        GoogleProfile profile = googleTokenVerifier.verify(request.credential());
        if (profile.email() == null || profile.email().isBlank()) {
            throw new InvalidCredentialsException("Google account email is missing");
        }

        String email = profile.email().toLowerCase();
        AppUser user = userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            String firstName = isBlank(profile.firstName()) ? deriveFirstName(email) : profile.firstName().trim();
            String lastName = isBlank(profile.lastName()) ? "Google" : profile.lastName().trim();
            AppUser created = AppUser.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.CUSTOMER)
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .enabled(true)
                    .build();
            return userRepository.saveAndFlush(created);
        });

        return new AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationMs(), toResponse(user));
    }
    public UserResponse getCurrentUser(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null || email.isBlank()) throw new InvalidCredentialsException("No authenticated user found");
        return toResponse(userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new InvalidCredentialsException("No authenticated user found")));
    }

    @Transactional
    public UserResponse updateCurrentUser(Authentication authentication, UpdateProfileRequest request) {
        AppUser user = authenticatedUser(authentication);
        if (request.firstName() != null && !request.firstName().isBlank()) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && !request.lastName().isBlank()) {
            user.setLastName(request.lastName().trim());
        }
        if (request.phoneNumber() != null && !request.phoneNumber().isBlank()) {
            String phone = request.phoneNumber().trim();
            if (userRepository.existsByPhoneNumber(phone) && !phone.equals(user.getPhoneNumber())) {
                throw new ResourceAlreadyExistsException("Phone number is already registered");
            }
            user.setPhoneNumber(phone);
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers(Authentication authentication) {
        requireAdmin(authentication);
        return userRepository.findAll().stream().map(this::toAdminResponse).toList();
    }

    @Transactional
    public AdminUserResponse updateUserRole(Authentication authentication, Long userId, UpdateRoleRequest request) {
        requireAdmin(authentication);
        AppUser user = userRepository.findById(userId).orElseThrow(() -> new InvalidCredentialsException("User not found"));
        if (user.getRole() == Role.ADMIN || request.role() == Role.ADMIN) {
            throw new AccessDeniedException("Admin role is locked to the seeded account");
        }
        user.setRole(request.role());
        AppUser saved = userRepository.save(user);
        notificationRepository.save(buildNotification(saved.getEmail(), null, "Access updated", "Your role was changed to " + saved.getRole() + ".", "ACCESS"));
        notificationRepository.save(buildNotification(null, Role.ADMIN, "Access updated", saved.getEmail() + " was changed to " + saved.getRole() + ".", "ACCESS"));
        publishNotification(saved.getEmail(), null, "Access updated", "Your role was changed to " + saved.getRole() + ".", "ACCESS");
        publishNotification(null, Role.ADMIN, "Access updated", saved.getEmail() + " was changed to " + saved.getRole() + ".", "ACCESS");
        return toAdminResponse(saved);
    }

    @Transactional
    public AdminUserResponse updateUserEnabled(Authentication authentication, Long userId, UpdateEnabledRequest request) {
        requireAdmin(authentication);
        AppUser user = userRepository.findById(userId).orElseThrow(() -> new InvalidCredentialsException("User not found"));
        if (user.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("Admin account access cannot be toggled");
        }
        user.setEnabled(Boolean.TRUE.equals(request.enabled()));
        user.setApprovalStatus(Boolean.TRUE.equals(request.enabled()) ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        AppUser saved = userRepository.save(user);
        notificationRepository.save(buildNotification(saved.getEmail(), null,
                saved.isEnabled() ? "Account activated" : "Account suspended",
                saved.isEnabled() ? "Your account access has been restored." : "Your account access has been temporarily suspended.",
                "ACCESS"));
        notificationRepository.save(buildNotification(null, Role.ADMIN,
                saved.isEnabled() ? "Account activated" : "Account suspended",
                saved.getEmail() + " was " + (saved.isEnabled() ? "enabled" : "disabled") + ".",
                "ACCESS"));
        publishNotification(saved.getEmail(), null,
                saved.isEnabled() ? "Account activated" : "Account suspended",
                saved.isEnabled() ? "Your account access has been restored." : "Your account access has been temporarily suspended.",
                "ACCESS");
        publishNotification(null, Role.ADMIN,
                saved.isEnabled() ? "Account activated" : "Account suspended",
                saved.getEmail() + " was " + (saved.isEnabled() ? "enabled" : "disabled") + ".",
                "ACCESS");
        return toAdminResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listNotifications(Authentication authentication) {
        AppUser user = authenticatedUser(authentication);
        return notificationRepository
                .findByRecipientEmailIgnoreCaseOrRecipientRoleOrderByCreatedAtDesc(user.getEmail(), user.getRole())
                .stream()
                .filter(notification -> notification.getRecipientEmail() == null || notification.getRecipientEmail().equalsIgnoreCase(user.getEmail()) || notification.getRecipientRole() == user.getRole())
                .map(this::toNotificationResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse createNotification(Authentication authentication, NotificationRequest request) {
        if ((request.recipientEmail() == null || request.recipientEmail().isBlank()) && request.recipientRole() == null) {
            throw new IllegalArgumentException("Notification needs a recipient email or role");
        }
        Notification notification = notificationRepository.save(buildNotification(
                request.recipientEmail() != null && !request.recipientEmail().isBlank() ? request.recipientEmail().trim().toLowerCase() : null,
                request.recipientRole(),
                request.title(),
                request.message(),
                request.category()
        ));
        publishNotification(notification.getRecipientEmail(), notification.getRecipientRole(), notification.getTitle(), notification.getMessage(), notification.getCategory());
        return toNotificationResponse(notification);
    }

    @Transactional
    public NotificationResponse markNotificationRead(Authentication authentication, Long notificationId) {
        AppUser user = authenticatedUser(authentication);
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new InvalidCredentialsException("Notification not found"));
        if (notification.getRecipientEmail() != null && !notification.getRecipientEmail().equalsIgnoreCase(user.getEmail())
                && notification.getRecipientRole() != user.getRole()) {
            throw new AccessDeniedException("You cannot modify this notification");
        }
        notification.setRead(true);
        notification.setReadAt(Instant.now());
        return toNotificationResponse(notificationRepository.save(notification));
    }

    private void requireAdmin(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new AccessDeniedException("Admin access required");
        }
    }

    private AppUser authenticatedUser(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : null;
        if (email == null || email.isBlank()) {
            throw new InvalidCredentialsException("No authenticated user found");
        }
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new InvalidCredentialsException("No authenticated user found"));
    }

    private AdminUserResponse toAdminResponse(AppUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getRestaurantId(),
                user.getRestaurantName(),
                user.getApprovalStatus(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getRecipientEmail(), notification.getRecipientRole(), notification.getTitle(), notification.getMessage(), notification.getCategory(), notification.isRead(), notification.getCreatedAt(), notification.getReadAt());
    }

    private Notification buildNotification(String recipientEmail, Role recipientRole, String title, String message, String category) {
        return Notification.builder()
                .recipientEmail(recipientEmail)
                .recipientRole(recipientRole)
                .title(title.trim())
                .message(message.trim())
                .category(category.trim().toUpperCase())
                .read(false)
                .build();
    }

    private void publishNotification(String recipientEmail, Role recipientRole, String title, String message, String category) {
        NotificationEvent event = new NotificationEvent(
                recipientEmail,
                recipientRole,
                title,
                message,
                category
        );
        notificationEventPublisher.publish(event);
    }
    public ValidationResponse validate(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return new ValidationResponse(false, null, null, null, null);
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            String role = userDetails.getAuthorities().stream().findFirst().map(a -> a.getAuthority().replaceFirst("^ROLE_", "")).orElse(null);
            AppUser user = userRepository.findByEmailIgnoreCase(userDetails.getUsername()).orElse(null);
            return new ValidationResponse(
                    true,
                    userDetails.getUsername(),
                    role,
                    user != null ? user.getRestaurantId() : null,
                    user != null && user.getApprovalStatus() != null ? user.getApprovalStatus().name() : null
            );
        }
        return new ValidationResponse(false, null, null, null, null);
    }
    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getRestaurantId(),
                user.getRestaurantName(),
                user.getApprovalStatus()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String deriveFirstName(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            return "Google";
        }
        String base = email.substring(0, at).replace('.', ' ').replace('_', ' ');
        if (base.isBlank()) {
            return "Google";
        }
        return Character.toUpperCase(base.charAt(0)) + base.substring(1);
    }
}
