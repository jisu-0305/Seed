package org.example.backend.controller.request.server;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HttpsConvertRequest {
    private Long projectId;
    private String domain;
    private String email;
}
