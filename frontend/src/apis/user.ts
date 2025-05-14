import { NotificationItem, NotificationResponse } from '@/types/notification';
import { ProjectMember } from '@/types/project';
import { MeResponse } from '@/types/user';

import { client } from './axios';

export async function fetchMe(): Promise<MeResponse> {
  const res = await client.get<MeResponse>('/users/me');
  return res.data;
}

export async function logout(): Promise<void> {
  await client.post('/users/logout');
}

export async function fetchNotifications(): Promise<NotificationItem[]> {
  const res = await client.get<NotificationResponse>('/notifications/unread');
  if (!res.data.success) {
    throw new Error(res.data.message || '알림 조회에 실패했습니다.');
  }
  return res.data.data;
}

export async function markNotificationRead(
  notificationId: number,
): Promise<void> {
  await client.patch(`/notifications/${notificationId}/read`);
}

export async function acceptInvitation(invitationId: number): Promise<void> {
  await client.post(`/invitations/${invitationId}/accept`);
}

/**
 * 초대 가능한 사용자 목록을 조회합니다.
 *
 * @param projectId 조회할 프로젝트 ID
 * @param keyword 검색어 (옵션)
 */
interface InvitationCandidatesResponse {
  success: boolean;
  message: string;
  data: ProjectMember[];
}

export async function fetchInvitationCandidates(
  projectId: number,
  keyword?: string,
): Promise<ProjectMember[]> {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const params: Record<string, any> = { projectId };
  if (keyword) params.keyword = keyword;

  const {
    data: { success, message, data },
  } = await client.get<InvitationCandidatesResponse>(
    '/invitations/candidates',
    { params },
  );

  if (!success) {
    throw new Error(message || '초대 가능 사용자 조회에 실패했습니다.');
  }

  return data;
}

/**
 * 다수의 사용자에게 초대 요청을 보냅니다.
 *
 * @param projectId 초대할 프로젝트 ID
 * @param idList 초대할 사용자 ID 배열
 */
export async function sendInvitations(
  projectId: number,
  idList: number[],
): Promise<void> {
  await client.post('/invitations', {
    projectId,
    idList,
  });
}

/**
 * 특정 프로젝트에 참여 중인 사용자 목록을 조회합니다.
 * @param projectId 프로젝트 ID
 * @returns ProjectMember 배열
 */
interface UserProjectsResponse {
  success: true;
  data: {
    projectId: number;
    users: ProjectMember[];
  };
}

export async function fetchProjectUsers(
  projectId: number,
): Promise<ProjectMember[]> {
  const {
    data: {
      success,
      data: { users },
    },
  } = await client.get<UserProjectsResponse>(
    `/user-projects/project/${projectId}`,
  );

  if (!success) {
    throw new Error('프로젝트 사용자 조회에 실패했습니다.');
  }

  return users;
}

/**
 * GitLab 개인 액세스 토큰(PAT)을 서버에 등록합니다.
 * @param token - 유저가 입력한 PAT 문자열
 */
export async function registerPat(token: string): Promise<void> {
  await client.post('/users/pat', token, {
    headers: {
      'Content-Type': 'text/plain',
    },
    // transformRequest를 지정하면 axios가 data를 JSON.stringify하지 않고 그대로 보냅니다.
    transformRequest: [(data) => data],
  });
}
