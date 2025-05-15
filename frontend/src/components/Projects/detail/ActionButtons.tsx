'use client';

import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';
import { useState } from 'react';

import { startBuild } from '@/apis/build';
import { convertServer } from '@/apis/server';
import { LoadingSpinner } from '@/components/Common/LoadingSpinner';
import ModalWrapper from '@/components/Common/Modal/ModalWrapper';
import { useModal } from '@/hooks/Common';
import { useThemeStore } from '@/stores/themeStore';
import { HttpsConfig } from '@/types/config';

import HttpsConfigModal from '../Modal/HttpsConfigModal';
import ManageMemberModal from '../Modal/ManageMemberModal';

interface ActionButtonsProps {
  projectId: string | null;
  gitlab?: string | URL;
  pemFilePath: string;
  httpsEnabled: boolean;
}

export function ActionButtons({
  projectId,
  gitlab,
  pemFilePath,
  httpsEnabled,
}: ActionButtonsProps) {
  const { mode } = useThemeStore();
  const team = useModal();
  const https = useModal();
  const [loading, setLoading] = useState(false);

  const router = useRouter();

  const goToReport = () => {
    if (!projectId) return;
    router.push(`/projects/${projectId}/report`);
  };

  const runBuild = async () => {
    if (!projectId) return;

    try {
      const data = await startBuild(projectId);
      console.log('✔️ 빌드 시작 성공:', data);
    } catch (err) {
      console.error('❌ 빌드 시작 실패:', err);
    }
  };

  const handleConfigSubmit = async ({ domain, email }: HttpsConfig) => {
    if (projectId == null) {
      console.error('projectId가 없습니다');
      https.toggle();
      return;
    }

    try {
      setLoading(true);
      const data = await convertServer(projectId, domain, email, pemFilePath);
      console.log('✔️ HTTPS 변환 요청 성공:', data);
    } catch (err) {
      console.error('❌ HTTPS 변환 요청 실패', err);
    } finally {
      setLoading(false);
      https.toggle();
      window.location.reload();
    }
  };

  const goToGitLab = () => {
    window.open(gitlab?.toString(), '_blank');
  };

  const goToEdit = () => {
    if (!projectId) return;
    router.push(`/projects/${projectId}/edit`);
  };

  if (mode === null) return null;

  return (
    <>
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
          <Button
            variant="https"
            onClick={https.toggle}
            disabled={httpsEnabled}
          >
            <Icon src="/assets/icons/ic_https_true_light.svg" alt="https" />
            Https 설정
          </Button>
        </MainActions>
        <SubActions>
          <SmallButton onClick={goToGitLab}>
            <SmallIcon
              src={`/assets/icons/ic_gitlab_${mode}.svg`}
              alt="gitlab"
            />{' '}
            GitLab
          </SmallButton>
          <SmallButton onClick={goToEdit}>
            <SmallIcon src={`/assets/icons/ic_edit_${mode}.svg`} alt="edit" />{' '}
            정보수정
          </SmallButton>
          <SmallButton onClick={team.toggle}>
            <SmallIcon src={`/assets/icons/ic_team_${mode}.svg`} alt="team" />{' '}
            팀원 관리
          </SmallButton>
        </SubActions>
      </Wrapper>
      <ModalWrapper isShowing={team.isShowing}>
        <ManageMemberModal
          projectId={projectId}
          isShowing={team.isShowing}
          handleClose={team.toggle}
        />
      </ModalWrapper>
      <ModalWrapper isShowing={https.isShowing}>
        {loading && <LoadingSpinner />}
        <HttpsConfigModal
          isShowing={https.isShowing}
          handleClose={https.toggle}
          onSubmit={handleConfigSubmit}
        />
      </ModalWrapper>
    </>
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

  &:disabled {
    opacity: 0.25;
    cursor: not-allowed;
    pointer-events: none;
  }
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
