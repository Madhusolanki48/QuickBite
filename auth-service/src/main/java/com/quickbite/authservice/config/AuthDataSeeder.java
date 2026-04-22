package com.quickbite.authservice.config;

import com.quickbite.authservice.model.AppUser;
import com.quickbite.authservice.model.ApprovalStatus;
import com.quickbite.authservice.model.Role;
import com.quickbite.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthDataSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        userRepository.deleteAllByRole(Role.ADMIN);
        seedAdmin();
        seedOwner("burger-palace-owner@quickbite.dev", "Aarav", "Mehta", "burger-palace", "Burger Palace",
                "Owner@1234");
        seedOwner("pizza-hut-owner@quickbite.dev", "Ishita", "Sharma", "pizza-hut-express", "Pizza Hut Express",
                "Owner@1234");
        seedOwner("sushi-zen-owner@quickbite.dev", "Ken", "Tanaka", "sushi-zen", "Sushi Zen", "Owner@1234");
        seedOwner("spice-garden-owner@quickbite.dev", "Meera", "Iyer", "spice-garden", "Spice Garden", "Owner@1234");
        seedOwner("taco-fiesta-owner@quickbite.dev", "Diego", "Lopez", "taco-fiesta", "Taco Fiesta", "Owner@1234");
        seedOwner("noodle-house-owner@quickbite.dev", "Lily", "Chen", "noodle-house", "Noodle House", "Owner@1234");
        seedDeliveryAgent("rahul.kumar@quickbite.dev", "Rahul", "Kumar", "Delivery@1234");
        seedDeliveryAgent("aisha.khan@quickbite.dev", "Aisha", "Khan", "Delivery@1234");
        seedDeliveryAgent("imran.ali@quickbite.dev", "Imran", "Ali", "Delivery@1234");
        seedDeliveryAgent("neha.singh@quickbite.dev", "Neha", "Singh", "Delivery@1234");
    }

    private void seedAdmin() {
        seedUser("admin@quickbite.dev", "Admin", "QuickBite", Role.ADMIN, null, null, "Admin@1234",
                ApprovalStatus.APPROVED, true);
    }

    private void seedOwner(String email, String firstName, String lastName, String restaurantId, String restaurantName,
            String rawPassword) {
        seedUser(email, firstName, lastName, Role.RESTAURANT_OWNER, restaurantId, restaurantName, rawPassword,
                ApprovalStatus.APPROVED, true);
    }

    private void seedDeliveryAgent(String email, String firstName, String lastName, String rawPassword) {
        seedUser(email, firstName, lastName, Role.DELIVERY_PARTNER, null, null, rawPassword, ApprovalStatus.APPROVED,
                true);
    }

    private void seedUser(
            String email,
            String firstName,
            String lastName,
            Role role,
            String restaurantId,
            String restaurantName,
            String rawPassword,
            ApprovalStatus approvalStatus,
            boolean enabled) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        AppUser user = AppUser.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .restaurantId(restaurantId)
                .restaurantName(restaurantName)
                .approvalStatus(approvalStatus)
                .enabled(enabled)
                .phoneNumber(null)
                .build();
        userRepository.save(user);
    }
}
