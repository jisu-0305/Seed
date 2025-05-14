import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';

import { createProject } from '@/apis/create';
import SmallButton from '@/components/Common/button/SmallButton';
import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';
import { PostProjectInfo } from '@/types/project';
import { getUrlFromId } from '@/utils/getProjectStep';

import InfoCheck from './InfoCheck';

export default function Complete() {
  const router = useRouter();
  const { mode } = useThemeStore();

  const { stepStatus, resetProjectStatus } = useProjectInfoStore();

  function stepStatusToProjectRequest(): PostProjectInfo {
    const { gitlab, server, app, env } = stepStatus;

    if (gitlab.structure === '모노') {
      return {
        structure: 'MONO',
        gitlab_project_id: gitlab.id,
        repositoryUrl: gitlab.repo,
        gitlabTargetBranch: gitlab.defaultBranch,
        backendDirectoryName: gitlab.directory.server,
        frontendDirectoryName: gitlab.directory.client,
        jdkVersion: env.jdk,
        serverIP: server.ip,
        frontendFramework: env.frontendFramework,
        nodejsVersion: env.node,
        jdkBuildTool: env.buildTool,
        applicationList: app,
      };
    }
    return {
      structure: 'MULTI',
      gitlab_project_id: gitlab.id,
      repositoryUrl: gitlab.repo,
      gitlabTargetBranch: gitlab.defaultBranch,
      backendBranchName: gitlab.directory.server,
      frontendBranchName: gitlab.directory.client,
      jdkVersion: env.jdk,
      serverIP: server.ip,
      frontendFramework: env.frontendFramework,
      nodejsVersion: env.node,
      jdkBuildTool: env.buildTool,
      applicationList: app,
    };
  }

  const handleSubmit = async () => {
    try {
      const projectInfo = stepStatusToProjectRequest();

      console.log(projectInfo);
      resetProjectStatus();

      const res = await createProject(projectInfo);

      if (res.success) {
        alert('프로젝트가 성공적으로 생성되었습니다!');
        router.push(`/dashboard`);
      }
    } catch (error) {
      console.error(error);
      alert('프로젝트 생성에 실패했습니다.');
    }
  };

  if (mode === null) return null;

  return (
    <Wrapper>
      <Header>
        <Title>프로젝트 정보 입력</Title>
        <ButtonWrapper>
          <SmallButton
            variant="cancel"
            onClick={() => router.push(getUrlFromId(4))}
          >
            <Icon
              src={`/assets/icons/ic_button_arrow_left_${mode}.svg`}
              alt="back"
            />
            이전
          </SmallButton>
          <SmallButton variant="complete" onClick={handleSubmit}>
            프로젝트 생성하기
            <Icon
              src="/assets/icons/ic_button_arrow_right_light.svg"
              alt="next"
            />
          </SmallButton>
        </ButtonWrapper>
      </Header>

      <MainPanel>
        <Aside>
          <SeedImage src="/assets/seed.png" alt="seed character" />
          <Message>
            <Highlight>씨앗</Highlight> 프로젝트를
            <br />
            시작하시겠습니까?
          </Message>
        </Aside>
        <InfoWrapper>
          <InfoCheck />
        </InfoWrapper>
      </MainPanel>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 2rem;

  width: 100%;
  margin: 0 auto;
`;

const Header = styled.div`
  display: flex;
  flex-direction: row;
  justify-content: space-between;
`;

const Title = styled.h3`
  ${({ theme }) => theme.fonts.Head0};
  font-size: 2.6rem;
`;

const ButtonWrapper = styled.div`
  display: flex;
  gap: 2rem;
`;

const Icon = styled.img``;

const MainPanel = styled.section`
  display: flex;
  flex-direction: row;
  justify-content: center;

  min-height: 55rem;
`;

const Aside = styled.aside`
  flex: 3;

  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  min-width: 20rem;
  max-width: 30rem;
  padding: 2rem;

  background-color: ${({ theme }) => theme.colors.Black};
  border-radius: 1.5rem;

  text-align: center;
`;

const SeedImage = styled.img`
  width: 100%;
  margin-bottom: 1rem;
`;

const Message = styled.span`
  ${({ theme }) => theme.fonts.Title3};
  color: ${({ theme }) => theme.colors.White};
`;

const Highlight = styled.span`
  ${({ theme }) => theme.fonts.Title3};
  color: ${({ theme }) => theme.colors.Main_Carrot};
`;

const InfoWrapper = styled.div`
  flex: 7;
`;
