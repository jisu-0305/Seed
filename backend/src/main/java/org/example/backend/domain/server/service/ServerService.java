package org.example.backend.domain.server.service;

import org.example.backend.controller.request.server.DeploymentRegistrationRequest;
import org.example.backend.controller.request.server.InitServerRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ServerService {
    void registerDeployment(DeploymentRegistrationRequest request, MultipartFile pemFile, MultipartFile frontEnvFile, MultipartFile backEnvFile, String accessToken);

    void resetServer(InitServerRequest request, MultipartFile pemFile);
}
