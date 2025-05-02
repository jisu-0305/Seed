'use client';

import styled from '@emotion/styled';

import { projects } from '@/assets/dummy/projects';
import { formatDateTime } from '@/utils/getFormattedTime';

import { ProjectCard } from './ProjectCard';

export default function Projects() {
  return (
    <SectionWrapper>
      <SectionTitle>Projects</SectionTitle>
      <CardsList>
        {projects.map((p, idx) => {
          const time = p.lastBuildAt
            ? formatDateTime(p.lastBuildAt)
            : '빌드 이력 없음';

          let emoji: 'default' | 'success' | 'fail';
          if (idx === 0) {
            emoji = 'default';
          } else if (p.lastBuildStatus === 'SUCCESS') {
            emoji = 'success';
          } else {
            emoji = 'fail';
          }

          return (
            <ProjectCard
              key={p.id}
              emoji={emoji}
              title={p.projectName}
              time={time}
              https={p.httpsEnabled}
              status={p.lastBuildStatus ?? ''}
              users={p.users}
            />
          );
        })}
      </CardsList>
    </SectionWrapper>
  );
}

const SectionWrapper = styled.section`
  padding: 2rem;
  background-color: ${({ theme }) => theme.colors.White};
  border-radius: 1.5rem;
  margin: 0 auto 4rem;
  max-width: 80rem;
`;

const SectionTitle = styled.h2`
  margin-bottom: 1.5rem;
  ${({ theme }) => theme.fonts.EnTitle1};
`;

const CardsList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;
