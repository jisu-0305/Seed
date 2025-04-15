package org.example.backend.controller.request.server;

public record DeleteServerFolderRequest(String ipAddress) {
    public static DeleteServerFolderRequest of(String ipAddress) {
        return new DeleteServerFolderRequest(ipAddress);
    }
}