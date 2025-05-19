package org.example.backend.domain.project.repository;

import org.example.backend.domain.project.entity.ApplicationEnvVariableList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationEnvVariableListRepository extends JpaRepository<ApplicationEnvVariableList, Long> {
    @Query("SELECT a.envVariableList FROM ApplicationEnvVariableList a WHERE a.applicationId = :applicationId")
    List<String> findEnvVariableListByApplicationId(@Param("applicationId") Long applicationId);
}
