package org.example.backend.common.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Properties;

@Slf4j
@Component
public class SshUtil {

    public Session createSessionWithPem(MultipartFile pemFile, String host) throws JSchException, IOException {
        byte[] keyBytes = pemFile.getBytes();
        JSch jsch = new JSch();
        jsch.addIdentity("ec2-key", keyBytes, null, null);

        Session session = jsch.getSession("ubuntu", host, 22);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect(10000);
        log.info("✅ SSH 연결 성공: {}", host);
        return session;
    }
}
