package org.example.backend.domain.server.service;

import org.example.backend.controller.request.server.HttpsConvertRequest;

public interface ServerService {
    void registerDeployment(Long projectId, String accessToken);

    void convertHttpToHttps(HttpsConvertRequest request, String accessToken);
}
