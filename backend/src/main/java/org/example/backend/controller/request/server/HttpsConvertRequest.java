package org.example.backend.controller.request.server;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HttpsConvertRequest {
    private String serverIP;
    private String domain;
    private String email;
    private Long projectId;

}
