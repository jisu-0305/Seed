package org.example.backend.domain.userproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_projects")
public class UserProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;

    private Long userId;

    public static UserProject create(Long projectId, Long userId) {
        return UserProject.builder()
                .projectId(projectId)
                .userId(userId)
                .build();
    }
}
