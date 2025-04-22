package org.example.backend.domain.userproject.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.example.backend.domain.userproject.entity.QUserProject;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserProjectCustomRepositoryImpl implements UserProjectCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public UserProjectCustomRepositoryImpl(EntityManager entityManager) {
        this.jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<Long> findUserIdsByProjectId(Long projectId) {
        QUserProject userProject = QUserProject.userProject;

        return jpaQueryFactory
                .select(userProject.userId)
                .from(userProject)
                .where(userProject.projectId.eq(projectId))
                .fetch();
    }
}
