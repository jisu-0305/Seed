package org.example.backend.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfile {
    private String userName;
    private String userIdentifyId;
    private String profileImageUrl;
}