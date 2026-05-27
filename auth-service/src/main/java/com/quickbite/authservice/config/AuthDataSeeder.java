package com.quickbite.authservice.config;

import com.quickbite.authservice.model.AppUser;
import com.quickbite.authservice.model.ApprovalStatus;
import com.quickbite.authservice.model.OnboardingStatus;
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
        userRepository.deleteAllByRole(Role.DELIVERY_PARTNER);
        seedAdmin();
        seedOwner("owner.urbanbites@quickbite.com", "Urban Bites", "Owner", "1", "Urban Bites", "QuickBite@123");
        seedOwner("owner.crustco@quickbite.com", "Crust & Co.", "Owner", "2", "Crust & Co.", "QuickBite@123");
        seedOwner("owner.royaltadka@quickbite.com", "Royal Tadka", "Owner", "3", "Royal Tadka", "QuickBite@123");
        seedOwner("owner.wokbowl@quickbite.com", "Wok & Bowl", "Owner", "4", "Wok & Bowl", "QuickBite@123");
        seedOwner("owner.greenspoon@quickbite.com", "Green Spoon", "Owner", "5", "Green Spoon", "QuickBite@123");
        seedOwner("owner.foodyard@quickbite.com", "The Food Yard", "Owner", "6", "The Food Yard", "QuickBite@123");
        seedDeliveryAgent("agent1@quickbite.com", "Jackson", "Ron", "QuickBite@123");
        seedDeliveryAgent("agent2@quickbite.com", "Paul", "Weasely", "QuickBite@123");
        seedDeliveryAgent("agent3@quickbite.com", "Olive", "Mandy", "QuickBite@123");
        seedDeliveryAgent("agent4@quickbite.com", "Edward", "Ford", "QuickBite@123");
        seedCustomer("customer@quickbite.com", "Demo", "Customer", "QuickBite@123");
    }

    private void seedAdmin() {
        seedUser("admin@quickbite.dev", "Admin", "QuickBite", Role.ADMIN, null, null, "Admin@1234",
                ApprovalStatus.APPROVED, OnboardingStatus.COMPLETED, true, true);
    }

    private void seedCustomer(String email, String firstName, String lastName, String rawPassword) {
        seedUser(email, firstName, lastName, Role.CUSTOMER, null, null, rawPassword, ApprovalStatus.APPROVED,
                OnboardingStatus.COMPLETED, true, true);
    }

    private void seedOwner(String email, String firstName, String lastName, String restaurantId, String restaurantName,
            String rawPassword) {
        seedUser(email, firstName, lastName, Role.RESTAURANT_OWNER, restaurantId, restaurantName, rawPassword,
                ApprovalStatus.APPROVED, OnboardingStatus.COMPLETED, true, true);
    }

    private void seedDeliveryAgent(String email, String firstName, String lastName, String rawPassword) {
        seedUser(email, firstName, lastName, Role.DELIVERY_PARTNER, null, null, rawPassword, ApprovalStatus.APPROVED,
                OnboardingStatus.COMPLETED, true, true);
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
            OnboardingStatus onboardingStatus,
            boolean enabled,
            boolean emailVerified) {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseGet(AppUser::new);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email.toLowerCase());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setRestaurantId(restaurantId);
        user.setRestaurantName(restaurantName);
        user.setApprovalStatus(approvalStatus);
        user.setOnboardingStatus(onboardingStatus);
        user.setEnabled(enabled);
        user.setEmailVerified(emailVerified);
        if (user.getPhoneNumber() == null) {
            user.setPhoneNumber(null);
        }
        userRepository.save(user);
    }
}
