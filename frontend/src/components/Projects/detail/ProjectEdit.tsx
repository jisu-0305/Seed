'use client';

import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';

import SmallButton from '@/components/Common/button/SmallButton';
import TipItem from '@/components/Common/TipItem';
import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';

import ProjectEditInput from './edit/ProjectEditInput';
import StepSidebar from './edit/StepSidebar';

export default function ProjectEdit() {
  const router = useRouter();
  const { mode } = useThemeStore();

  const { stepStatus, setServerStatus, setAppStatus, setEnvStatus } =
    useProjectInfoStore();

  return (
    <Wrapper>
      <TitleHeader>
        <BackIcon
          src={`/assets/icons/ic_back_${mode}.svg`}
          alt="뒤로가기"
          onClick={() => router.back()}
        />
        <Heading>프로젝트 정보 수정</Heading>
        <TipItem text="최종 수정 완료 후 재빌드를 해주세요!" important />
      </TitleHeader>
      <Content>
        <Main>
          <ProjectEditInput
            server={stepStatus.server}
            env={stepStatus.env}
            apps={stepStatus.app}
            onChangeServer={setServerStatus}
            onChangeEnv={setEnvStatus}
            onChangeApps={setAppStatus}
          />
        </Main>
        <SideBarWrapper>
          <StepSidebar
            gitlab={stepStatus.gitlab}
            server={stepStatus.server}
            apps={stepStatus.app}
            env={stepStatus.env}
          />

          <StButtonWrapper>
            <SmallButton
              variant="cancel"
              onClick={() => {
                router.back();
              }}
            >
              취소
            </SmallButton>
            <SmallButton
              variant="next"
              onClick={() => {
                router.back();
              }}
            >
              완료
            </SmallButton>
          </StButtonWrapper>
        </SideBarWrapper>
      </Content>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  max-width: 100rem;

  display: flex;
  flex-direction: column;

  margin: 0 auto;
  padding: 2rem;
  padding-top: 5rem;
`;

const TitleHeader = styled.div`
  display: flex;
  justify-content: flex-start;
  align-items: center;
  width: 100%;
  max-height: 70rem;
  max-width: 110rem;
  margin-bottom: 3rem;
  gap: 2rem;
`;

const BackIcon = styled.img`
  width: 1.5rem;
  cursor: pointer;
`;

const Heading = styled.h2`
  ${({ theme }) => theme.fonts.EnTitle1};
`;

const Content = styled.div`
  display: flex;
  flex: 1;
  gap: 2rem;
`;

const Main = styled.main`
  flex: 6;
  display: flex;
  align-items: center;

  min-height: 50rem;

  /* padding: 0 3rem 2rem; */

  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1.5rem;
`;

const SideBarWrapper = styled.aside`
  flex: 4;
`;

const StButtonWrapper = styled.aside`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 2rem;

  padding-top: 3rem;
`;
