package org.example.backend.controller.request.server;

public record NewServerRequest(
        String ipAddress,
        String keyFilePath
) {
    public static NewServerRequest of(String ipAddress) {
        return new NewServerRequest(ipAddress, null);
    }

    public NewServerRequest withKeyFilePath(String path) {
        return new NewServerRequest(this.ipAddress, path);
    }
}

