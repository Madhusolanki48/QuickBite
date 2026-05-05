package com.quickbite.authservice.repository;

import com.quickbite.authservice.model.Notification;
import com.quickbite.authservice.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientEmailIgnoreCaseOrRecipientRoleOrderByCreatedAtDesc(String recipientEmail,
            Role recipientRole);

    List<Notification> findByRecipientRoleOrderByCreatedAtDesc(Role recipientRole);

    List<Notification> findByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(String recipientEmail);

    Optional<Notification> findByIdAndRecipientEmailIgnoreCase(Long id, String recipientEmail);
}
