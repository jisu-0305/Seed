package org.example.backend.common.util;

public class JenkinsUriBuilder {

    private JenkinsUriBuilder() {}

    public static String buildBuildInfoUri(String jobName, String path) {
        return String.format("/job/%s/%s", jobName, path);
    }

    public static String buildConsoleLogUri(String jobName, int buildNumber) {
        return String.format("/job/%s/%d/consoleText", jobName, buildNumber);
    }

    public static String buildTriggerUri(String jobName) {
        return String.format("/job/%s/build", jobName);
    }
}
