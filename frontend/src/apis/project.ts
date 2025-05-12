import { useQuery } from '@tanstack/react-query';

import {
  ProjectCardInfo,
  ProjectDetailData,
  ProjectDetailResponse,
  ProjectSummary,
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
  const { data } = await client.get('/projects/status');
  return data.data;
}

export function useProjectCards() {
  return useQuery({
    queryKey: ['project-cards'],
    queryFn: getProjects,
  });
}
