package org.example.backend.domain.userproject.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInProject {
    private Long userId;
    private String userName;
    private String userIdentifyId;
    private String profileImageUrl;
}
