import { NotificationItem, NotificationResponse } from '@/types/notification';
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

export async function acceptInvitation(invitationId: number): Promise<void> {
  await client.post(`/invitations/${invitationId}/accept`);
}

export async function markNotificationRead(
  notificationId: number,
): Promise<void> {
  await client.patch(`/notifications/${notificationId}/read`);
}
