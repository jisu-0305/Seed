package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageName;

    private int defaultPort;

    @ElementCollection
    @CollectionTable(
            name = "application_env",
            joinColumns = @JoinColumn(name = "application_id")
    )
    @MapKeyColumn(name = "env_key")
    @Column(name = "env_value")
    private Map<String, String> envVariables = new HashMap<>();
}