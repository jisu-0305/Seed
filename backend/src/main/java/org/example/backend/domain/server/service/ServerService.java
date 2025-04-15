package org.example.backend.domain.server.service;

import org.example.backend.controller.request.server.DeleteServerFolderRequest;
import org.example.backend.controller.request.server.NewServerRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ServerService {
    void registerServer(NewServerRequest newServerRequest, MultipartFile keyFile) throws IOException;

    void deleteFolderOnServer(DeleteServerFolderRequest request);
}
