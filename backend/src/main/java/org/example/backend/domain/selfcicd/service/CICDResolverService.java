package org.example.backend.domain.selfcicd.service;

import org.example.backend.domain.selfcicd.enums.FailType;

public interface CICDResolverService {
    void handleSelfHealingCI(Long projectId, String accessToken, String failType);
}
