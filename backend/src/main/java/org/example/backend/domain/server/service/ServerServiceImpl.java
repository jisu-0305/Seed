package org.example.backend.domain.server.service;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.RequiredArgsConstructor;
import org.example.backend.controller.request.server.NewServerRequest;
import org.example.backend.domain.server.entity.ServerInfo;
import org.example.backend.domain.server.repository.ServerInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {

    private final ServerInfoRepository repository;

    public void registerServer(NewServerRequest newServerRequest, MultipartFile keyFile) throws IOException {
        // 1. key.pem 저장
        String dirPath = System.getProperty("user.dir") + "/keys/";
        new File(dirPath).mkdirs();
        String filePath = dirPath + UUID.randomUUID() + "_" + keyFile.getOriginalFilename();
        File savedFile = new File(filePath);
        keyFile.transferTo(savedFile);

        // 1-1. chmod 400 유사한 파일 권한 제한
        savedFile.setReadable(true, true);      // 소유자만 읽기 가능
        savedFile.setWritable(false, false);    // 쓰기 금지
        savedFile.setExecutable(false, false);  // 실행 금지

        // 2. request + key 경로 → DTO 업데이트
        NewServerRequest completedRequest = newServerRequest.withKeyFilePath(filePath);

        // 3. Entity 생성
        ServerInfo server = ServerInfo.create(completedRequest.ipAddress(), completedRequest.keyFilePath());

        // 4. 저장
        repository.save(server);

        // 5. EC2 SSH 폴더 생성
        createFolderOnEC2(server.getIpAddress(), server.getKeyFilePath(), "/home/ubuntu/test-folder");
    }

    private void createFolderOnEC2(String ip, String pemPath, String folderPath) {
        try {
            JSch jsch = new JSch();
            jsch.addIdentity(pemPath);

            Session session = jsch.getSession("ubuntu", ip, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // 1. 폴더 만들기 + 2. hello.txt 만들기
            String commands = String.join(" && ", List.of(
                    "mkdir -p " + folderPath,
                    "echo 'Hello World' > " + folderPath + "/hello.txt"
            ));

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(commands);
            channel.connect();

            // 종료
            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            throw new RuntimeException("EC2 폴더 또는 파일 생성 실패", e);
        }
    }
}

