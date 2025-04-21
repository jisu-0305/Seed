package org.example.backend.common.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.backend.domain.user.enums.ProviderType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SessionInfoDto {
    private Long userId;
    private String oauthId;
    private ProviderType providerType;
}
