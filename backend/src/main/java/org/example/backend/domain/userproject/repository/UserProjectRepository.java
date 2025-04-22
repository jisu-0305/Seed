package org.example.backend.domain.userproject.repository;


import org.example.backend.domain.userproject.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserProjectRepository extends JpaRepository<UserProject, Long> {
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    List<UserProject> findByProjectId(Long projectId);
    List<UserProject> findByUserId(Long userId);
    void deleteAllByProjectId(Long projectId);

    @Query("SELECT up.userId FROM UserProject up WHERE up.projectId = :projectId")
    List<Long> findUserIdsByProjectId(@Param("projectId") Long projectId);
}
