package com.quickbite.authservice.repository;

import com.quickbite.authservice.model.EmailToken;
import com.quickbite.authservice.model.EmailTokenPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {
    Optional<EmailToken> findByEmailIgnoreCaseAndTokenAndPurposeAndUsedAtIsNullAndExpiresAtAfter(
            String email,
            String token,
            EmailTokenPurpose purpose,
            Instant now);

    void deleteByEmailIgnoreCaseAndPurposeAndUsedAtIsNull(String email, EmailTokenPurpose purpose);

    void deleteByEmailIgnoreCaseAndPurpose(String email, EmailTokenPurpose purpose);

    Optional<EmailToken> findFirstByEmailIgnoreCaseAndPurposeAndUsedAtIsNullAndExpiresAtAfter(
            String email,
            EmailTokenPurpose purpose,
            Instant now);
}
