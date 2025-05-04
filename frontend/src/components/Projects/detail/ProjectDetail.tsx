import styled from '@emotion/styled';
import { useParams } from 'next/navigation';

import { project, tasksByTab } from '@/assets/dummy/project_detail';

import { AvatarList } from '../AvatarList';
import { ActionButtons } from './ActionButtons';
import { DeployStatus } from './deployStatus/DeployStatus';
import { ProjectHeader } from './ProjectHeader';
import { ProjectInfo } from './ProjectInfo';

export default function ProjectDetail() {
  const params = useParams();
  const id = params?.id;
  console.log('프로젝트 상세: ', id);

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

          <ActionButtons />
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
  padding-top: 7rem;
  background-color: ${({ theme }) => theme.colors.White};
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
