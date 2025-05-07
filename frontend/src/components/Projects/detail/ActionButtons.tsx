/* eslint-disable no-nested-ternary */
import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';

import { useThemeStore } from '@/stores/themeStore';

interface ActionButtonsProps {
  projectId: string | undefined;
}

export function ActionButtons({ projectId }: ActionButtonsProps) {
  const { mode } = useThemeStore();

  const router = useRouter();

  const goToReport = () => {
    if (!projectId) return;
    router.push(`/projects/${projectId}/report`);
  };

  const runBuild = async () => {
    console.log('빌드 다시 시작하기 API 연결하기');
  };

  const runHttps = async () => {
    console.log('Https 설정 시작하기 API 연결하기');
  };

  const goToGitLab = () => {
    window.open('https://lab.ssafy.com/s12-final/S12P31A206', '_blank');
  };

  const goToEdit = () => {
    if (!projectId) return;
    router.push(`/projects/${projectId}/edit`);
  };

  return (
    <Wrapper>
      <MainActions>
        <Button variant="ai" onClick={goToReport}>
          <Icon src="/assets/icons/ic_ai_report_carrot.svg" alt="ai_report" />
          AI 보고서
        </Button>
        <Button variant="build" onClick={runBuild}>
          <Icon src="/assets/icons/ic_build_dark.svg" alt="build_now" />
          지금 빌드
        </Button>
        <Button variant="https" onClick={runHttps}>
          <Icon src="/assets/icons/ic_https_true_light.svg" alt="https" />
          Https 설정
        </Button>
      </MainActions>
      <SubActions>
        <SmallButton onClick={goToGitLab}>
          <SmallIcon src={`/assets/icons/ic_gitlab_${mode}.svg`} alt="gitlab" />{' '}
          GitLab
        </SmallButton>
        <SmallButton onClick={goToEdit}>
          <SmallIcon src={`/assets/icons/ic_edit_${mode}.svg`} alt="edit" />{' '}
          정보수정
        </SmallButton>
        <SmallButton>
          <SmallIcon src={`/assets/icons/ic_team_${mode}.svg`} alt="team" />{' '}
          팀원 관리
        </SmallButton>
      </SubActions>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1rem;
`;

const MainActions = styled.div`
  display: flex;
  gap: 1rem;
`;

const SubActions = styled.div`
  display: flex;
  gap: 1rem;
`;

type Variant = 'ai' | 'build' | 'https';

const Button = styled.button<{ variant: Variant }>`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-width: 13rem;
  height: auto;
  gap: 0.5rem;
  padding: 3rem 2rem;
  border: none;
  border-radius: 1.5rem;
  background: ${({ variant, theme }) =>
    variant === 'ai'
      ? theme.colors.Carrot2
      : variant === 'build'
        ? theme.colors.Blue3
        : theme.colors.Blue4};
  color: ${({ theme }) => theme.colors.Black};
  ${({ theme }) => theme.fonts.Title4};
`;

const Icon = styled.img`
  width: 3rem;
`;

const SmallButton = styled.button`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  min-width: 13rem;
  height: auto;
  gap: 0.5rem;
  padding: 0.7rem 1rem;
  border: none;
  border-radius: 1.5rem;
  background: ${({ theme }) => theme.colors.Text};
  color: ${({ theme }) => theme.colors.Background};
  ${({ theme }) => theme.fonts.Body2};
`;

const SmallIcon = styled.img`
  width: 2rem;
`;
