'use client';

import styled from '@emotion/styled';

import { formatDateTime } from '@/utils/getFormattedTime';

import { ProjectCard } from './ProjectCard';

export default function Projects() {
  // dummy
  const projects = [
    {
      id: 1,
      projectName: 'K-ing Service',
      httpsEnabled: true,
      autoDeployEnabled: true,
      lastBuildStatus: 'SUCCESS',
      lastBuildAt: '2025-05-01T14:32:10Z',
      users: [
        { id: 101, name: '김예슬', avatarUrl: '/assets/user.png' },
        { id: 102, name: '이준호', avatarUrl: '/assets/user.png' },
        { id: 103, name: '박지민', avatarUrl: '/assets/user.png' },
      ],
    },
    {
      id: 2,
      projectName: 'Alpha API',
      httpsEnabled: false,
      autoDeployEnabled: true,
      lastBuildStatus: 'FAILURE',
      lastBuildAt: '2025-04-30T09:15:47Z',
      users: [
        { id: 104, name: '최수빈', avatarUrl: '/assets/user.png' },
        { id: 105, name: '정우성', avatarUrl: '/assets/user.png' },
      ],
    },
    {
      id: 3,
      projectName: 'Gamma Frontend',
      httpsEnabled: true,
      autoDeployEnabled: false,
      lastBuildStatus: null,
      lastBuildAt: null,
      users: [],
    },
    {
      id: 4,
      projectName: 'Delta Worker',
      httpsEnabled: true,
      autoDeployEnabled: true,
      lastBuildStatus: 'SUCCESS',
      lastBuildAt: '2025-05-02T11:22:33Z',
      users: [
        { id: 106, name: '이소정', avatarUrl: '/assets/user.png' },
        { id: 107, name: '박준영', avatarUrl: '/assets/user.png' },
        { id: 108, name: '한지원', avatarUrl: '/assets/user.png' },
        { id: 109, name: '한지민', avatarUrl: '/assets/user.png' },
      ],
    },
  ];

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
