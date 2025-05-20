import styled from '@emotion/styled';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import {
  fetchBuildDetail,
  fetchHttpsBuildLogs,
  fetchLastBuild,
  HttpsBuildLog,
} from '@/apis/build';
import { fetchProjectDetail } from '@/apis/project';
import ErrorMessage from '@/components/Common/ErrorMessage';
import { LoadingSpinner } from '@/components/Common/LoadingSpinner';
import { useProjectStatusPolling } from '@/hooks/Common/useProjectStatusPolling';
import { useProjectInfoStore, useProjectStore } from '@/stores/projectStore';
import type { DeployTabName } from '@/types/deploy';
import { DeployTabNames } from '@/types/deploy';
import { ProjectDetailData, ProjectSummary } from '@/types/project';
import type { Task, TaskStatus } from '@/types/task';
import { formatDateTime } from '@/utils/getFormattedTime';

import { AvatarList } from '../AvatarList';
import { ActionButtons } from './ActionButtons';
import { DeployStatus } from './deployStatus/DeployStatus';
import { ProjectHeader } from './ProjectHeader';
import { ProjectInfo } from './ProjectInfo';

export default function ProjectDetail() {
  const params = useParams();
  const rawId = params?.id;
  const projectId = Array.isArray(rawId) ? rawId[0] : rawId;
  const loadProjectInfo = useProjectInfoStore((s) => s.loadProjectInfo);
  const { restartPolling } = useProjectStatusPolling(projectId);

  const router = useRouter();

  const { projects, loadProjects } = useProjectStore();
  const [selectedTab, setSelectedTab] = useState<DeployTabName>(
    DeployTabNames[0],
  );
  const [defaultBuildNumber, setdefaultBuildNumber] = useState<number | null>(
    null,
  );

  const [errorMessage, setErrorMessage] = useState('아직 빌드기록이 없습니다');

  const [detail, setDetail] = useState<
    ProjectDetailData &
      Pick<
        ProjectSummary,
        | 'memberList'
        | 'autoDeploymentEnabled'
        | 'httpsEnabled'
        | 'buildStatus'
        | 'lastBuildAt'
      >
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  >(null as any);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadProjects();
  }, [loadProjects]);

  useEffect(() => {
    if (!projectId) {
      router.replace('/projects');
      return;
    }
    const id = Number(projectId);
    setLoading(true);
    fetchAndSetDetail(id);
  }, [projectId, projects]);

  const fetchAndSetDetail = async (id: number) => {
    setLoading(true);
    try {
      const data = await fetchProjectDetail(id);
      loadProjectInfo(data);
      restartPolling(); // 서버 상태 정보 받아오기

      const summary = projects.find((p) => p.id === id);
      setDetail({
        ...data,
        // summary에서 가져오던 필드들도 동일하게 설정
        memberList: summary?.memberList ?? [],
        httpsEnabled: summary?.httpsEnabled ?? false,
        autoDeploymentEnabled: summary?.autoDeploymentEnabled ?? false,
        buildStatus: summary?.buildStatus ?? null,
        lastBuildAt: summary?.lastBuildAt ?? null,
      });
    } catch {
      setError('프로젝트 상세 정보를 불러오는 데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleHttpsComplete = () => {
    if (!projectId) return;
    fetchAndSetDetail(Number(projectId));
    refreshTasks();
  };
  const handleDeployComplete = () => {
    if (!projectId) return;
    fetchAndSetDetail(Number(projectId));
    refreshTasks();
  };

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

  const refreshTasks = async () => {
    if (!projectId) return;
    const id = Number(projectId);
    if (Number.isNaN(id)) return;

    if (selectedTab === 'Https 세팅') {
      fetchHttpsBuildLogs(id)
        .then((logs: HttpsBuildLog[]) => {
          const tasks: Task[] = logs.map<Task>((log) => ({
            stepNumber: log.stepNumber,
            stepName: log.stepName,
            duration: new Date(log.createdAt).toLocaleTimeString(),
            status: log.status as TaskStatus,
          }));
          setTasksByTab((prev) => ({ ...prev, [selectedTab]: tasks }));
        })
        .catch((err) => {
          console.error('HTTPS 빌드 로그 로딩 실패', err);
          setTasksByTab((prev) => ({ ...prev, [selectedTab]: [] }));
        });
    } else if (selectedTab === '최근 빌드') {
      // 1) 최근 빌드 요약 호출
      fetchLastBuild(id)
        .then((summary) => {
          setdefaultBuildNumber(summary.buildNumber);
          return fetchBuildDetail(id, summary.buildNumber);
        })
        .then((tasks) => {
          setTasksByTab((prev) => ({ ...prev, [selectedTab]: tasks }));
        })
        .catch((err) => {
          if (err.response && err.response.status === 500) {
            setErrorMessage(`${err.response.data.message}`);
          } else {
            console.error('요청 중 에러 발생:', err);
          }
          setTasksByTab((prev) => ({ ...prev, [selectedTab]: [] }));
        });
    }
  };

  useEffect(() => {
    refreshTasks();
  }, [projectId, selectedTab]);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorMessage>{error}</ErrorMessage>;
  if (!detail) return null;

  let emoji: 'default' | 'success' | 'fail';
  if (detail.buildStatus === 'SUCCESS') {
    emoji = 'success';
  } else {
    emoji = 'fail';
  }

  const time = detail.lastBuildAt
    ? formatDateTime(detail.lastBuildAt)
    : '빌드 이력 없음';

  return (
    <SectionWrapper>
      <Section>
        <SectionTitle>
          <Icon src="/assets/icons/ic_gitlab.svg" alt="gitlab" />
          <Title>{detail.projectName}</Title>
          <AvatarList users={detail.memberList} maxVisible={2} />
        </SectionTitle>
        <SectionInfo>
          <ProjectHeader
            emojiSrc={`/assets/projectcard/project_${emoji}.png`}
            https={detail.httpsEnabled}
            deploy={detail.autoDeploymentEnabled}
            buildStatus={detail.buildStatus}
            lastBuildAt={time}
          />

          <ProjectInfo
            folder={detail.structure}
            clientDir={
              detail.structure === 'MONO'
                ? detail.frontendDirectoryName
                : detail.frontendBranchName
            }
            serverDir={
              detail.structure === 'MONO'
                ? detail.backendDirectoryName
                : detail.backendBranchName
            }
            nodeVersion={detail.nodejsVersion}
            jdkVersion={detail.jdkVersion}
            buildTool={detail.jdkBuildTool}
          />

          <ActionButtons
            projectId={projectId}
            gitlab={detail.repositoryUrl}
            httpsEnabled={detail.httpsEnabled}
            deployEnabled={detail.autoDeploymentEnabled}
            onHttpsComplete={handleHttpsComplete}
            onDeployComplete={handleDeployComplete}
          />
        </SectionInfo>
        <SubTitle>Deploy Status</SubTitle>
        <DeployStatus
          projectId={projectId}
          buildNumber={defaultBuildNumber}
          tasksByTab={tasksByTab}
          selectedTab={selectedTab}
          onTabChange={setSelectedTab}
          errorMessage={errorMessage}
        />
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
