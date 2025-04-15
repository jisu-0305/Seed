package org.example.backend.domain.server.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServerInfo {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;
    private String keyFilePath;

    public static ServerInfo create(String ipAddress, String keyFilePath) {
        ServerInfo entity = new ServerInfo();
        entity.ipAddress = ipAddress;
        entity.keyFilePath = keyFilePath;
        return entity;
    }
}
