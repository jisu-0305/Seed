// apis/build.ts
import { client } from './axios';

export interface HttpsBuildLog {
  stepNumber: number;
  stepName: string;
  logContent: string;
  createdAt: string;
}

interface HttpsBuildLogsResponse {
  success: boolean;
  message: string;
  data: HttpsBuildLog[];
}

/**
 * 특정 프로젝트의 HTTPS 빌드 로그(스텝) 목록을 가져옵니다.
 *
 * @param projectId 조회할 프로젝트 ID
 * @returns HttpsBuildLog 배열
 */
export async function fetchHttpsBuildLogs(
  projectId: number,
): Promise<HttpsBuildLog[]> {
  const {
    data: { success, message, data },
  } = await client.get<HttpsBuildLogsResponse>(`/server/${projectId}`);

  if (!success) {
    throw new Error(message || 'HTTPS 빌드 로그 조회에 실패했습니다.');
  }

  return data;
}
