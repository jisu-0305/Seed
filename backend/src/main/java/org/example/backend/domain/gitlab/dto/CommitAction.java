package org.example.backend.domain.gitlab.dto;

import lombok.Data;

@Data
public class CommitAction {
    private String action;
    private String file_path;
    private String content;
}
