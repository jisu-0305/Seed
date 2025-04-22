package org.example.backend.domain.project.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.backend.domain.project.entity.QProject;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectCustomRepositoryImpl implements ProjectCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public ProjectCustomRepositoryImpl(EntityManager entityManager) {
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public String findProjectNameById(Long projectId) {
        QProject project = QProject.project;

        return jpaQueryFactory
                .select(project.projectName)
                .from(project)
                .where(project.id.eq(projectId))
                .fetchOne();
    }
}