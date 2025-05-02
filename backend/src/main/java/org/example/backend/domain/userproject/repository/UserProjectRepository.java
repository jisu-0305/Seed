package org.example.backend.domain.userproject.repository;


import org.example.backend.domain.userproject.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProjectRepository extends JpaRepository<UserProject, Long>, UserProjectCustomRepository{
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);
    List<UserProject> findByProjectId(Long projectId);
    List<UserProject> findByUserId(Long userId);
    void deleteAllByProjectId(Long projectId);
    List<UserProject> findByProjectIdIn(List<Long> projectIdList);
}
