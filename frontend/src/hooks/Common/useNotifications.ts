import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import {
  acceptInvitation,
  fetchNotifications,
  markNotificationRead,
} from '@/apis/user';
import type { NotificationItem } from '@/types/notification';

export function useNotifications() {
  const qc = useQueryClient();

  const {
    data: notifications = [],
    isLoading,
    error,
  } = useQuery<NotificationItem[]>({
    queryKey: ['notifications'],
    queryFn: fetchNotifications,
    staleTime: 1000 * 60 * 5,
  });

  const removeFromCache = (id: number) => {
    qc.setQueryData<NotificationItem[]>(['notifications'], (old = []) =>
      old.filter((n) => n.id !== id),
    );
  };

  const markRead = useMutation({
    mutationFn: (id: number) => markNotificationRead(id),
    onMutate: async (id: number) => {
      await qc.cancelQueries({ queryKey: ['notifications'] });
      const previous = qc.getQueryData<NotificationItem[]>(['notifications']);
      removeFromCache(id);
      return { previous };
    },
    onError: (_err, _id, context) => {
      if (context?.previous) {
        qc.setQueryData(['notifications'], context.previous);
      }
    },
  });

  const accept = useMutation<
    void,
    Error,
    { notificationId: number; invitationId: number },
    { previous?: NotificationItem[] }
  >({
    mutationFn: async ({ notificationId, invitationId }) => {
      await acceptInvitation(invitationId);
      await markNotificationRead(notificationId);
    },
    onMutate: async (variables) => {
      const { notificationId } = variables;
      await qc.cancelQueries({ queryKey: ['notifications'] });
      const previous = qc.getQueryData<NotificationItem[]>(['notifications']);
      // 낙관적 업데이트: 해당 알림만 제거
      if (previous) {
        qc.setQueryData<NotificationItem[]>(
          ['notifications'],
          previous.filter((n) => n.id !== notificationId),
        );
      }
      return { previous };
    },
    onError: (_err, _id, context) => {
      if (context?.previous) {
        qc.setQueryData(['notifications'], context.previous);
      }
    },
  });

  return { notifications, isLoading, error, markRead, accept };
}
