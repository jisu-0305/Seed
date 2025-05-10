package org.example.backend.util.backoffice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.example.backend.domain.gitlab.dto.GitlabCompareDiff;

import java.util.List;
import java.util.Map;

@Data
public class SimulationRequestDto {
    private String accessToken;
    private Long projectId;
    private String jenkinsErrorLog;
    private List<String> applicationNames;
    private List<Map<String, String>> tree;
    private List<GitlabCompareDiff> gitDiff;
    private Map<String, String> appLogs;
}
