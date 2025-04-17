package org.example.backend.domain.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitlabUser {
    private String id;
}
