package org.example.backend.domain.gitlab.repository;

import org.example.backend.domain.gitlab.entity.GitlabToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GitlabTokenRepository extends JpaRepository<GitlabToken, Long> {

    Optional<GitlabToken> findByUser_Id(Long userId);

//    boolean existsByUserId(Long userId);
}
