package org.example.backend.domain.jenkins.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jenkins_info")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class JenkinsInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private String baseUrl;
    private String username;
    private String apiToken;
    private String jobName;
}
