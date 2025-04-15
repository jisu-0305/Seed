package org.example.backend.domain.server.repository;

import org.example.backend.domain.server.entity.ServerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerInfoRepository extends JpaRepository<ServerInfo, Long> {
}

