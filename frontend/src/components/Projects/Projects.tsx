'use client';

import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';
import { useEffect } from 'react';

import { useProjectStore } from '@/stores/projectStore';
import { formatDateTime } from '@/utils/getFormattedTime';

import { ProjectCard } from './ProjectCard';

export default function Projects() {
  const router = useRouter();
  const { projects, loading, error, loadProjects } = useProjectStore();

  useEffect(() => {
    loadProjects();
  }, [loadProjects]);

  if (loading) return <p>로딩 중…</p>;
  if (error) return <p>{error}</p>;

  if (projects.length === 0) {
    return (
      <SectionWrapper>
        <SectionTitle>
          <Title>Projects</Title>
        </SectionTitle>
        <Message>아직 프로젝트가 없습니다. 프로젝트를 생성해주세요.</Message>
        <CreateButton onClick={() => router.push('/create/gitlab')}>
          프로젝트 생성하기
        </CreateButton>
      </SectionWrapper>
    );
  }

  return (
    <SectionWrapper>
      <SectionTitle>
        <Title>Projects</Title>
      </SectionTitle>
      <Section>
        <CardsList>
          {projects.map((p, idx) => {
            const time = p.lastBuildAt
              ? formatDateTime(p.lastBuildAt)
              : '빌드 이력 없음';

            let emoji: 'default' | 'success' | 'fail';
            if (idx === 0) {
              emoji = 'default';
            } else if (p.buildStatus === 'SUCCESS') {
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
                status={p.buildStatus ?? ''}
                users={p.memberList}
              />
            );
          })}
        </CardsList>
      </Section>
    </SectionWrapper>
  );
}

const SectionWrapper = styled.section`
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  width: 100%;
  padding: 7rem 2rem;
  border-radius: 1.5rem;
`;

const Section = styled.div`
  max-height: 61rem;
  padding: 1rem;
  overflow-y: auto;

  & {
    scrollbar-width: thin;
    scrollbar-color: ${({ theme }) =>
      `${theme.colors.BorderDefault} transparent`};
  }
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

const Message = styled.div`
  text-align: center;
  min-width: 80rem;
  ${({ theme }) => theme.fonts.Body2};
`;

const CardsList = styled.div`
  display: flex;
  flex-direction: column;
  min-width: 80rem;
  gap: 3rem;
`;

const CreateButton = styled.button`
  margin-top: 1.5rem;
  padding: 1rem 1.5rem;
  ${({ theme }) => theme.fonts.Title5};
  background-color: ${({ theme }) => theme.colors.Main_Carrot};
  color: white;
  border: none;
  border-radius: 5rem;
  cursor: pointer;

  &:hover {
    opacity: 0.9;
  }
`;
