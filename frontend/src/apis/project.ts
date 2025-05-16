import { useQuery } from '@tanstack/react-query';

// eslint-disable-next-line import/no-cycle
import { useProjectFileStore } from '@/stores/projectStore';
import { ExecutionsByDate, ExecutionsResponse } from '@/types/execution';
import {
  ProjectCardInfo,
  ProjectDetailData,
  ProjectDetailResponse,
  ProjectSummary,
  ProjectUpdateRequest,
} from '@/types/project';

import { client } from './axios';

interface ProjectListResponse {
  success: boolean;
  message: string;
  data: ProjectSummary[];
}

/**
 * 프로젝트 목록을 조회합니다.
 */
export async function fetchProjects(): Promise<ProjectSummary[]> {
  const res = await client.get<ProjectListResponse>('/projects');

  if (!res.data.success) {
    throw new Error(res.data.message || '프로젝트 목록 조회에 실패했습니다.');
  }
  return res.data.data;
}

/**
 * 특정 프로젝트의 상세 정보를 조회합니다.
 */
export async function fetchProjectDetail(
  projectId: number,
): Promise<ProjectDetailData> {
  const res = await client.get<ProjectDetailResponse>(
    `/projects/${projectId}/detail`,
  );
  if (!res.data.success) {
    throw new Error(res.data.message || '프로젝트 상세 조회에 실패했습니다.');
  }
  return res.data.data;
}

// 대시보드 프로젝트 목록 조회
export async function getProjects(): Promise<ProjectCardInfo[]> {
  const { data } = await client.get('/projects');
  return data.data;
}

export function useProjectCards() {
  return useQuery({
    queryKey: ['project-cards'],
    queryFn: getProjects,
  });
}

export const fetchProjectExecutions = async (): Promise<ExecutionsByDate[]> => {
  const res = await client.get<ExecutionsResponse>('/projects/executions');
  return res.data.data;
};

export const useProjectExecutions = () => {
  return useQuery<ExecutionsByDate[], Error>({
    queryKey: ['projectExecutions'],
    queryFn: fetchProjectExecutions,
  });
};

/**
 * 프로젝트를 삭제합니다.
 * @param projectId 삭제할 프로젝트 ID
 */
export async function deleteProject(projectId: number): Promise<void> {
  await client.delete(`/projects/${projectId}`);
}

/**
 * 프로젝트를 부분 수정합니다.
 * @param projectId          수정할 프로젝트 ID
 * @param projectRequest     서버 IP 및 application 목록
 */
export async function updateProject(
  projectId: number,
  projectUpdateRequest: ProjectUpdateRequest,
) {
  const { frontEnvFile, backEnvFile } = useProjectFileStore.getState();
  const formData = new FormData();

  formData.append(
    'projectUpdateRequest',
    new Blob([JSON.stringify(projectUpdateRequest)], {
      type: 'application/json',
    }),
  );

  if (frontEnvFile) {
    formData.append('clientEnvFile', frontEnvFile, frontEnvFile.name);
  }
  if (backEnvFile) {
    formData.append('serverEnvFile', backEnvFile, backEnvFile.name);
  }

  return client.patch(`/projects/${projectId}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}
