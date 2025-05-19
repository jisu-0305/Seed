package org.example.backend.domain.server.service;

import org.example.backend.controller.request.server.HttpsConvertRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ServerService {
    void registerDeployment(Long projectId, MultipartFile pemFile, String accessToken);

    void convertHttpToHttps(HttpsConvertRequest request, MultipartFile pemFile, String accessToken);
}
