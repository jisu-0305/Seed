package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.ApplicationEnvVariables;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationEnvVariableListRepository extends JpaRepository<ApplicationEnvVariables, Long> {

    @Query("SELECT a.envVariable FROM ApplicationEnvVariables a WHERE a.applicationId = :applicationId")
    List<String> findEnvVariablesByApplicationId(@Param("applicationId") Long applicationId);
}
