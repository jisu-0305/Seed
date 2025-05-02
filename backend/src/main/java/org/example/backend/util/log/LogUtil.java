package org.example.backend.util.log;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LogUtil {

    public static String getRecentDockerLogs(String ip, String pemPath, String containerName, String sinceDuration) {
        StringBuilder log = new StringBuilder();

        try {
            JSch jsch = new JSch();
            jsch.addIdentity(pemPath);

            Session session = jsch.getSession("ubuntu", ip, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            String command = String.format("docker logs --since %s %s", sinceDuration, containerName);
            channel.setCommand(command);

            InputStream input = channel.getInputStream();
            channel.connect();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.append(line).append("\n");
                }
            }

            channel.disconnect();
            session.disconnect();

            return log.toString();

        } catch (Exception e) {
            throw new RuntimeException("도커 로그 가져오기 실패", e);
        }
    }
}
