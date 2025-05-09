package org.example.backend.domain.userproject.repository;

import org.example.backend.domain.userproject.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    boolean existsByProjectIdAndReceiverId(Long projectId, Long receiverId);
    List<Invitation> findByReceiverIdAndExpiresAtAfter(Long receiverId, LocalDateTime now);

    /** 해당 프로젝트에 속한 모든 Invitation 조회 */
    List<Invitation> findAllByProjectId(Long projectId);

    /** 특정 유저가 받은 초대를 단건 조회 (없으면 Optional.empty()) */
    Optional<Invitation> findByProjectIdAndReceiverId(Long projectId, Long receiverId);

}
