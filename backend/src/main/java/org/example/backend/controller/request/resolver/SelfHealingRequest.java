package org.example.backend.controller.request.resolver;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SelfHealingRequest {

    @NotNull(message = "빌드 번호는 필수입니다.")
    private Integer buildNumber;
}

