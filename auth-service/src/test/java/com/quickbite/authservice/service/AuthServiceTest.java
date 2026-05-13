package com.quickbite.authservice.service;

import com.quickbite.authservice.dto.GoogleLoginRequest;
import com.quickbite.authservice.dto.GoogleProfile;
import com.quickbite.authservice.dto.LoginRequest;
import com.quickbite.authservice.dto.RegisterRequest;
import com.quickbite.authservice.model.AppUser;
import com.quickbite.authservice.model.Role;
import com.quickbite.authservice.messaging.NotificationEventPublisher;
import com.quickbite.authservice.repository.EmailTokenRepository;
import com.quickbite.authservice.repository.NotificationRepository;
import com.quickbite.authservice.repository.UserRepository;
import com.quickbite.authservice.security.GoogleTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private EmailTokenRepository emailTokenRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private NotificationEventPublisher notificationEventPublisher;
    @Mock private MailService mailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private GoogleTokenVerifier googleTokenVerifier;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, emailTokenRepository, notificationRepository, notificationEventPublisher, mailService, passwordEncoder, jwtService, authenticationManager, googleTokenVerifier);
    }

    @Test
    void registerCreatesUserAndReturnsToken() {
        when(userRepository.existsByEmailIgnoreCase("sonam@example.com")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("9876543210")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded");
        when(userRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        var response = authService.register(new RegisterRequest("Sonam", "Sharma", "sonam@example.com", "9876543210", "Password123", Role.CUSTOMER, null));

        assertThat(response.token()).isNull();
        assertThat(response.user().email()).isEqualTo("sonam@example.com");
        verify(userRepository).saveAndFlush(any(AppUser.class));
        verify(mailService).sendRegistrationOtp(any(AppUser.class), any());
    }

    @Test
    void registerRejectsAdminRole() {
        assertThatThrownBy(() -> authService.register(new RegisterRequest("Sonam", "Sharma", "sonam@example.com", "9876543210", "Password123", Role.ADMIN, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Admin accounts cannot be registered");
    }

    @Test
    void loginAuthenticatesAndReturnsToken() {
        AppUser user = AppUser.builder()
                .id(2L)
                .firstName("Sonam")
                .lastName("Sharma")
                .email("sonam@example.com")
                .password("encoded")
                .role(Role.CUSTOMER)
                .emailVerified(true)
                .enabled(true)
                .build();

        when(userRepository.findByEmailIgnoreCase("sonam@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        var response = authService.login(new LoginRequest("sonam@example.com", "Password123"));

        assertThat(response.token()).isEqualTo("jwt");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void googleLoginCreatesCustomerIfMissing() {
        when(googleTokenVerifier.verify("google-credential")).thenReturn(new GoogleProfile("sub", "newuser@example.com", "New", "User", null, true));
        when(userRepository.findByEmailIgnoreCase("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.saveAndFlush(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(5L);
            return user;
        });
        when(jwtService.generateToken(any(AppUser.class))).thenReturn("google-jwt");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        var response = authService.googleLogin(new GoogleLoginRequest("google-credential"));

        assertThat(response.token()).isEqualTo("google-jwt");
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(captor.getValue().getEmail()).isEqualTo("newuser@example.com");
    }
}
