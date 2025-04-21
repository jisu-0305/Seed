package org.example.backend.domain.userproject.repository;

import org.example.backend.domain.userproject.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    boolean existsByProjectIdAndReceiverId(Long projectId, Long receiverId);
    List<Invitation> findByReceiverId(Long receiverId);
}
