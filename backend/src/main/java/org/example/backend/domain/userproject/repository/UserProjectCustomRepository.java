package org.example.backend.domain.userproject.repository;

import java.util.List;

public interface UserProjectCustomRepository {
    List<Long> findUserIdsByProjectId(Long projectId);
}
