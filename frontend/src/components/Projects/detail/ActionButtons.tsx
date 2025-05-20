'use client';

import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';
import { useEffect, useMemo, useState } from 'react';

import { convertServer, startBuildWithPem } from '@/apis/server';
import { LoadingSpinner } from '@/components/Common/LoadingSpinner';
import ModalWrapper from '@/components/Common/Modal/ModalWrapper';
import { useModal } from '@/hooks/Common';
import { useProjectStatusPolling } from '@/hooks/Common/useProjectStatusPolling';
import { useThemeStore } from '@/stores/themeStore';
import { EC2Config, HttpsConfig } from '@/types/config';
import { SERVER_STATUS_INFO } from '@/utils/getStatusMessage';

import HttpsConfigModal from '../Modal/HttpsConfigModal';
import ManageMemberModal from '../Modal/ManageMemberModal';
import PemModal from '../Modal/PemModal';
import ServerStatusBar from '../ServerStatusBar';

interface ActionButtonsProps {
  projectId: string;
  gitlab?: string | URL;
  httpsEnabled: boolean;
  deployEnabled: boolean;
  isBuildLoading: boolean;
  isHttpsLoading: boolean;

  onHttpsComplete?: () => void;
  onDeployComplete?: () => void;
}

export function ActionButtons({
  projectId,
  gitlab,
  httpsEnabled,
  deployEnabled,
  isBuildLoading,
  isHttpsLoading,
  onHttpsComplete,
  onDeployComplete,
}: ActionButtonsProps) {
  const { mode } = useThemeStore();
  const team = useModal();
  const https = useModal();
  const build = useModal();

  const { status, restartPolling } = useProjectStatusPolling(projectId);
  const statusInfo = useMemo(() => SERVER_STATUS_INFO[status ?? ''], [status]);

  const isBuildFinished =
    statusInfo?.category === 'build' && status === 'FINISH';
  const isHttpsFinished =
    statusInfo?.category === 'https' && status === 'FINISH_CONVERT_HTTPS';

  // https 모달용
  const [isHttpsDisabled, setIsHttpsDisabled] = useState(httpsEnabled);
  const [HttpsLoading, setHttpsLoading] = useState(isHttpsLoading);

  // ■ 빌드용 로딩 & 메시지
  const [isBuildDisabled, setIsBuildDisabled] = useState(deployEnabled);
  const [buildLoading, setBuildLoading] = useState(isBuildLoading);

  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const router = useRouter();

  const goToReport = () => {
    if (!projectId) return;
    router.push(`/projects/${projectId}/report`);
  };

  // const runBuild = async () => {
  //   if (!projectId) return;

  //   // 메시지 띄우기
  //   setBuildLoading(true);
  //   setErrorMessage(null);

  //   try {
  //     const data = await startBuild(projectId);
  //     console.log('✔️ EC2 세팅 성공:', data);
  //     setIsBuildDisabled(true);
  //     onDeployComplete?.();
  //   } catch (err) {
  //     console.error('❌ EC2 세팅 실패:', err);
  //     setErrorMessage('EC2 세팅 중 오류가 발생했어요. 다시 시도해주세요.');
  //   } finally {
  //     setBuildLoading(false);
  //   }
  // };

  useEffect(() => {
    setIsBuildDisabled(deployEnabled);
  }, [deployEnabled]);

  const handleConfigSubmit = async ({ pem, domain, email }: HttpsConfig) => {
    if (projectId == null) {
      console.error('projectId가 없습니다');
      https.toggle();
      return;
    }

    setHttpsLoading(true);
    setErrorMessage(null);
    https.toggle();

    restartPolling('https');
    try {
      const data = await convertServer(projectId, domain, email, pem);
      console.log('✔️ HTTPS 변환 요청 성공:', data);
      setIsHttpsDisabled(true);
      onHttpsComplete?.();
    } catch (err) {
      console.error('❌ HTTPS 변환 요청 실패', err);
      setErrorMessage('HTTPS 설정 중 오류가 발생했어요.');
    } finally {
      setHttpsLoading(false);
    }
  };

  const handlePemSubmit = async ({ pem }: EC2Config) => {
    if (!projectId) {
      console.error('projectId가 없습니다');
      build.toggle();
      return;
    }

    setBuildLoading(true);
    setErrorMessage(null);
    build.toggle();

    restartPolling('build');
    try {
      const data = await startBuildWithPem(projectId, pem);
      console.log('✔️ EC2 세팅 성공:', data);
      setIsBuildDisabled(true);
      onDeployComplete?.();
    } catch (err) {
      console.error('❌ EC2 세팅 실패:', err);
      setErrorMessage('EC2 세팅 중 오류가 발생했어요.');
    } finally {
      setBuildLoading(false);
    }
  };

  useEffect(() => {
    setIsHttpsDisabled(httpsEnabled);
  }, [httpsEnabled]);

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
        {errorMessage && (
          <MessageBanner>
            <BannerMessage>{errorMessage}</BannerMessage>
            <IcIcon
              src="/assets/icons/ic_close.svg"
              alt="close icon"
              onClick={() => setErrorMessage(null)}
            />
          </MessageBanner>
        )}
        <ServerStatusBar status={status!} />
        <MainActions>
          <Button variant="ai" onClick={goToReport}>
            <Icon src="/assets/icons/ic_ai_report_carrot.svg" alt="ai_report" />
            AI 보고서
          </Button>
          <Button
            variant="build"
            onClick={build.toggle}
            disabled={buildLoading || isBuildDisabled || isBuildFinished}
          >
            {buildLoading ? (
              <LoadingSpinner />
            ) : (
              <Icon src="/assets/icons/ic_build_dark.svg" alt="ec2" />
            )}
            EC2 세팅
          </Button>
          <Button
            variant="https"
            onClick={https.toggle}
            disabled={isHttpsDisabled || HttpsLoading || isHttpsFinished}
          >
            {HttpsLoading ? (
              <LoadingSpinner />
            ) : (
              <Icon src="/assets/icons/ic_https_true_light.svg" alt="https" />
            )}
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
      <ModalWrapper isShowing={https.isShowing || build.isShowing}>
        {(HttpsLoading || buildLoading) && <LoadingSpinner />}
        <HttpsConfigModal
          isShowing={https.isShowing}
          handleClose={https.toggle}
          onSubmit={handleConfigSubmit}
        />
        <PemModal
          isShowing={build.isShowing}
          handleClose={build.toggle}
          onSubmit={handlePemSubmit}
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

// 메시지 배너 스타일
const MessageBanner = styled.div`
  position: relative;
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: center;
  padding: 1rem 2rem;
  background: ${({ theme }) => theme.colors.RedBtn};
  border-radius: 5rem;
`;

const BannerMessage = styled.div`
  ${({ theme }) => theme.fonts.Body2};
  color: ${({ theme }) => theme.colors.MenuText};
`;

const IcIcon = styled.img`
  position: absolute;
  right: 1rem;
  cursor: pointer;
`;
