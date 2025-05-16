package org.example.backend.controller.response.gitlab;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.domain.gitlab.dto.GitlabProject;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitlabProjectListResponse {
    @JsonProperty("projects")
    private List<GitlabProject> gitlabProjectList;
}
