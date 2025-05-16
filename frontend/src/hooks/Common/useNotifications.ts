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
    staleTime: 1000 * 60 * 5, // 5분
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

  const accept = useMutation({
    mutationFn: async (id: number) => {
      await acceptInvitation(id);
      await markNotificationRead(id);
    },
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

  return { notifications, isLoading, error, markRead, accept };
}
