import styled from '@emotion/styled';
import { useParams, useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';

import { fetchProjectDetail } from '@/apis/project';
import { tasksByTab as dummyTasks } from '@/assets/dummy/builds';
import { useProjectStore } from '@/stores/projectStore';
import type { DeployTabName } from '@/types/deploy';
import { DeployTabNames } from '@/types/deploy';
import { ProjectDetailData, ProjectSummary } from '@/types/project';
import type { Task } from '@/types/task';
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

  const router = useRouter();

  const { projects, loadProjects } = useProjectStore();

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
    const summary = projects.find((p) => p.id === id);
    setLoading(true);

    fetchProjectDetail(id)
      .then((data) => {
        setDetail({
          ...data,
          memberList: summary?.memberList ?? [],
          autoDeploymentEnabled: summary?.autoDeploymentEnabled ?? false,
          httpsEnabled: summary?.httpsEnabled ?? false,
          buildStatus: summary?.buildStatus ?? null,
          lastBuildAt: summary?.lastBuildAt ?? null,
        });
      })
      .catch((err) => {
        console.error(err);
        setError('프로젝트 상세 정보를 불러오는 데 실패했습니다.');
      })
      .finally(() => {
        setLoading(false);
      });
  }, [projectId, projects, router]);

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

  if (loading) return <p>로딩 중…</p>;
  if (error) return <p>{error}</p>;
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
            buildStatus={detail.buildStatus}
            lastBuildAt={time}
          />

          <ProjectInfo
            folder={detail.structure}
            clientDir={detail.frontendDirectoryName}
            serverDir={detail.backendDirectoryName}
            nodeVersion={detail.nodejsVersion}
            jdkVersion={detail.jdkVersion}
            buildTool={detail.jdkBuildTool}
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
