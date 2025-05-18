package org.example.backend.controller.request.fcm;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FCMRequest {
    private Long projectId;
    private String template;
}
