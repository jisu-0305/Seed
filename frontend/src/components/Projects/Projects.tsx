'use client';

import styled from '@emotion/styled';

import { projects } from '@/assets/dummy/projects';
import { formatDateTime } from '@/utils/getFormattedTime';

import { ProjectCard } from './ProjectCard';

export default function Projects() {
  return (
    <SectionWrapper>
      <SectionTitle>
        <Title>Projects</Title>
      </SectionTitle>
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
              id={p.id}
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
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  width: 100%;
  padding-top: 10rem;
  background-color: ${({ theme }) => theme.colors.White};
  border-radius: 1.5rem;
`;

const SectionTitle = styled.h2`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  padding-left: 4rem;
  margin-bottom: 2.5rem;
`;

const Title = styled.div`
  min-width: 80rem;
  ${({ theme }) => theme.fonts.EnTitle1};
`;

const CardsList = styled.div`
  display: flex;
  flex-direction: column;
  min-width: 80rem;
  gap: 3rem;
`;
