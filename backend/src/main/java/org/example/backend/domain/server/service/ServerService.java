package org.example.backend.domain.server.service;

import org.example.backend.controller.request.server.DeploymentRegistrationRequest;
import org.example.backend.controller.request.server.HttpsConvertRequest;

public interface ServerService {
    void registerDeployment(DeploymentRegistrationRequest request, String accessToken);

    void convertHttpToHttps(HttpsConvertRequest request, String pemFilePath, String accessToken);
}
