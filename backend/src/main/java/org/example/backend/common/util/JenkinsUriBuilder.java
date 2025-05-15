package org.example.backend.common.util;

public class JenkinsUriBuilder {

    private JenkinsUriBuilder() {}

    public static String buildBuildInfoUri(String baseUrl, String jobName, String path) {
        return String.format("%s/job/%s/%s", baseUrl, jobName, path);
    }

    public static String buildConsoleLogUri(String baseUrl, String jobName, int buildNumber) {
        return String.format("%s/job/%s/%d/consoleText", baseUrl, jobName, buildNumber);
    }

    public static String buildTriggerUri(String baseUrl, String jobName) {
        return baseUrl + "/job/" + jobName + "/buildWithParameters";
    }
}
