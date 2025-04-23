package org.example.backend.domain.userproject.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInProject {
    private Long userId;
    private String name;
    private String username;
    private String avatarUrl;
}