package com.quickbite.authservice.repository;

import com.quickbite.authservice.model.AppUser;
import com.quickbite.authservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    void deleteAllByRole(Role role);
}
