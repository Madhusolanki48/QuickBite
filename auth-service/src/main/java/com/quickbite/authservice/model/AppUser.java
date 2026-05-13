package com.quickbite.authservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String firstName;
    @Column(nullable = false, length = 50)
    private String lastName;
    @Column(nullable = false, unique = true, length = 120)
    private String email;
    @Column(unique = true, length = 20)
    private String phoneNumber;
    @Column(length = 80)
    private String restaurantId;
    @Column(length = 120)
    private String restaurantName;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus approvalStatus;
    @Column(nullable = false)
    private boolean emailVerified;
    @Column(nullable = false)
    private boolean enabled;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();
        if (approvalStatus == null)
            approvalStatus = ApprovalStatus.APPROVED;
    }
}
