// apis/project.ts

import { ProjectSummary } from '@/types/project';

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
