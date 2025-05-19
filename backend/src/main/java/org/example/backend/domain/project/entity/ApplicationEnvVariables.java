package org.example.backend.domain.project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "application_env_variables")
public class ApplicationEnvVariables {

    @Id
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "env_variable")
    private String envVariable;
}
