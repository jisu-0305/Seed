package org.example.backend.domain.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "application_env_variable_list")
public class ApplicationEnvVariableList {


    @Id
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "env_variable_list")
    private String envVariableList;

    protected ApplicationEnvVariableList() {}
}
