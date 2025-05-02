import styled from '@emotion/styled';

import { project, tasksByTab } from '@/assets/dummy/project_detail';

import { AvatarList } from '../AvatarList';
import { ActionButtons } from './ActionButtons';
import { DeployStatus } from './DeployStatus';
import { ProjectHeader } from './ProjectHeader';
import { ProjectInfo } from './ProjectInfo';

export default function ProjectDetail() {
  return (
    <SectionWrapper>
      <Top>
        <Icon src="/assets/icons/ic_gitlab.svg" alt="gitlab" />
        <Title>{project.projectName}</Title>
        <AvatarList users={project.users} maxVisible={2} />
      </Top>
      <Section>
        <ProjectHeader
          emojiSrc={project.emojiSrc}
          https={project.httpsEnabled}
          lastBuildStatus={project.lastBuildStatus}
          lastBuildTime={project.lastBuildTime}
        />

        <ProjectInfo
          clientDir={project.projectInfo.clientDir}
          serverDir={project.projectInfo.serverDir}
          nodeVersion={project.projectInfo.nodeVersion}
          jdkVersion={project.projectInfo.jdkVersion}
          buildTool={project.projectInfo.buildTool}
        />

        <ActionButtons />
      </Section>

      <DeployStatus tasksByTab={tasksByTab} />
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

const Section = styled.div`
  display: flex;
  gap: 2rem;
  margin-top: 2rem;
`;

const Top = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
`;

const Title = styled.h1`
  margin: 0;
  ${({ theme }) => theme.fonts.EnTitle1};
`;

const Icon = styled.img`
  width: 3rem;
  height: 3rem;
`;
