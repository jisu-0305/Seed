// apis/build.ts
import type { EchoList, Task, TaskStatus } from '@/types/task';

import { client } from './axios';

export interface HttpsBuildLog {
  stepNumber: number;
  stepName: string;
  logContent: string;
  status: string;
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

export interface BuildSummary {
  buildNumber: number;
  buildName: string;
  date: string;
  time: string;
  status: string;
}

export interface BuildListResponse {
  builds: BuildSummary[];
  hasNext: boolean;
  nextStart: number;
}

export interface BuildDetailResponse {
  buildNumber: number;
  buildName: string;
  overallStatus: string;
  stepList: Array<{
    stepNumber: number;
    stepName: string;
    duration: string;
    status: string;
    echoList?: EchoList[];
  }>;
}

/**
 * 해당 프로젝트의 빌드 목록을 커서(start) 기준으로 가져옵니다.
 */
export async function fetchBuilds(
  projectId: number,
  start = 0,
  limit = 20,
): Promise<BuildListResponse> {
  const res = await client.get<BuildListResponse>(
    `/jenkins/${projectId}/builds`,
    { params: { start, limit } },
  );
  return res.data;
}

/**
 * 특정 빌드의 상세(스텝) 정보를 가져와 Task[] 타입으로 변환합니다.
 */
function toTaskStatus(s: string): TaskStatus {
  if (s === 'SUCCESS' || s === 'FAIL' || s === 'FAILED' || s === '-') return s;
  console.warn(`Unknown status "${s}", defaulting to "-"`);
  return '-';
}

export async function fetchBuildDetail(
  projectId: number,
  buildNumber: number,
): Promise<Task[]> {
  const res = await client.get<BuildDetailResponse>(
    `/jenkins/${projectId}/builds/${buildNumber}`,
  );
  // stepList를 Task 타입에 맞춰 매핑
  return res.data.stepList.map<Task>((step) => ({
    stepNumber: step.stepNumber,
    stepName: step.stepName,
    duration: step.duration,
    status: toTaskStatus(step.status),
    echoList: step.echoList,
  }));
}

/**
 * 특정 빌드의 로그를 텍스트로 가져옵니다.
 */
export async function fetchBuildLogs(
  projectId: number,
  buildNumber: number,
): Promise<string> {
  const res = await client.get<string>(
    `/jenkins/${projectId}/builds/${buildNumber}/log`,
    { responseType: 'text' },
  );
  return res.data;
}

/**
 * 특정 빌드의 특정 스텝 로그를 텍스트로 가져옵니다.
 */
export async function fetchStepLog(
  projectId: number,
  buildNumber: number,
  stepNumber: number,
): Promise<string> {
  const res = await client.get<string>(
    `/jenkins/${projectId}/builds/${buildNumber}/${stepNumber}`,
    { responseType: 'text' },
  );
  return res.data;
}

/**
 * 가장 최근 빌드 하나를 요약 정보로 가져옵니다.
 */
export async function fetchLastBuild(projectId: number): Promise<BuildSummary> {
  const res = await client.get<BuildSummary>(
    `/jenkins/${projectId}/builds/last`,
  );
  return res.data;
}

// 재훈이 API
export async function startBuild(projectId: string) {
  const res = await client.post('/server/deployment', null, {
    params: { projectId },
  });
  return res.data;
}
