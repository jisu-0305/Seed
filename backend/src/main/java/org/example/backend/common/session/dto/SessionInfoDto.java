package org.example.backend.common.session.dto;

import lombok.Builder;
import lombok.Data;
import org.example.backend.domain.user.enums.ProviderType;

@Data
@Builder
public class SessionInfoDto {
    private Long userId;
    private String oauthId;
    private ProviderType providerType;
}
