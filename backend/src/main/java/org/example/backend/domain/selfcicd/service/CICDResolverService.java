package org.example.backend.domain.selfcicd.service;

public interface CICDResolverService {
    void handleSelfHealingCI(Long projectId, String accessToken);
}
