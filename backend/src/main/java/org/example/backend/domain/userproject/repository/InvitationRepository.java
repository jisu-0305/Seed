package org.example.backend.domain.userproject.repository;

import org.example.backend.domain.userproject.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    boolean existsByProjectIdAndReceiverId(Long projectId, Long receiverId);
    List<Invitation> findByReceiverIdAndExpiresAtAfter(Long receiverId, LocalDateTime now);
}
