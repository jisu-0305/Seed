import styled from '@emotion/styled';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';

import { tasksByTab as dummyTasks } from '@/assets/dummy/builds';
import { project } from '@/assets/dummy/project_detail';
import type { DeployTabName } from '@/types/deploy';
import { DeployTabNames } from '@/types/deploy';
import type { Task } from '@/types/task';

import { AvatarList } from '../AvatarList';
import { ActionButtons } from './ActionButtons';
import { DeployStatus } from './deployStatus/DeployStatus';
import { ProjectHeader } from './ProjectHeader';
import { ProjectInfo } from './ProjectInfo';

export default function ProjectDetail() {
  const params = useParams();
  const rawId = params?.id;
  const projectId = Array.isArray(rawId) ? rawId[0] : rawId;

  // 1) state로 관리
  const [tasksByTab, setTasksByTab] = useState<Record<DeployTabName, Task[]>>(
    // 초기에는 모든 탭 빈 배열
    DeployTabNames.reduce(
      (acc, tab) => {
        acc[tab] = [];
        return acc;
      },
      {} as Record<DeployTabName, Task[]>,
    ),
  );

  // 2) 마운트 시에 "API 호출" (여기서는 더미)
  useEffect(() => {
    // 실제 API 호출이라면 fetch(...) 후 json 파싱
    // 예: fetch(`/api/projects/${id}/deploy-tasks`).then(res => res.json()).then(data => setTasksByTab(data));

    // 더미 데이터를 가져와서 500ms 뒤에 세팅
    const timer = setTimeout(() => {
      setTasksByTab(dummyTasks);
    }, 500);

    return () => clearTimeout(timer);
  }, [projectId]);

  return (
    <SectionWrapper>
      <Section>
        <SectionTitle>
          <Icon src="/assets/icons/ic_gitlab.svg" alt="gitlab" />
          <Title>{project.projectName}</Title>
          <AvatarList users={project.users} maxVisible={2} />
        </SectionTitle>
        <SectionInfo>
          <ProjectHeader
            emojiSrc={project.emojiSrc}
            https={project.httpsEnabled}
            lastBuildStatus={project.lastBuildStatus}
            lastBuildTime={project.lastBuildTime}
          />

          <ProjectInfo
            folder={project.projectInfo.folder}
            clientDir={project.projectInfo.clientDir}
            serverDir={project.projectInfo.serverDir}
            nodeVersion={project.projectInfo.nodeVersion}
            jdkVersion={project.projectInfo.jdkVersion}
            buildTool={project.projectInfo.buildTool}
          />

          <ActionButtons projectId={projectId} />
        </SectionInfo>

        <SubTitle>Deploy Status</SubTitle>
        <DeployStatus tasksByTab={tasksByTab} />
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
  padding: 5rem 2rem;
  border-radius: 1.5rem;
`;

const Section = styled.div`
  display: flex;
  flex-direction: column;
  min-width: 100rem;
  max-width: 120rem;
`;

const SectionTitle = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: center;
  gap: 2rem;
  margin-bottom: 1rem;
`;

const SectionInfo = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 4rem;
  margin-top: 2rem;
`;

const Title = styled.h1`
  margin: 0;
  ${({ theme }) => theme.fonts.EnTitle0};
`;

const SubTitle = styled.h1`
  margin-top: 5rem;
  ${({ theme }) => theme.fonts.EnTitle1};
`;

const Icon = styled.img`
  width: 3rem;
  height: 3rem;
`;
