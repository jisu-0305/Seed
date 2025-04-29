import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5분 동안 fresh
      retry: 1, // 실패하면 1번 재시도
      refetchOnWindowFocus: false, // 창 포커스될 때 refetch X
    },
    mutations: {
      retry: 0, // mutation 실패해도 재시도 X
    },
  },
});
