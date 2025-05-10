// src/store/userStore.ts
import { create } from 'zustand';
import { createJSONStorage, persist } from 'zustand/middleware';

import { fetchMe } from '@/apis/user';
import { MeResponse } from '@/types/user';

interface UserState {
  user: MeResponse['data'] | null;
  fetchUser: () => Promise<void>;
  clearUser: () => void;
  hasHydrated: boolean; // 리하이드레이션 완료 여부
  markHydrated: () => void; // 리하이드레이션 완료 마킹
}

export const useUserStore = create<UserState>()(
  persist(
    (set) => ({
      user: null,
      hasHydrated: false,
      markHydrated: () => set({ hasHydrated: true }),

      fetchUser: async () => {
        try {
          const res = await fetchMe();
          console.log('유저 정보 API 호출');

          set({ user: res.data });
        } catch (err) {
          console.error(err);
          set({ user: null });
        }
      },

      clearUser: () => set({ user: null }),
    }),
    {
      name: 'user',
      storage: createJSONStorage(() => sessionStorage),
      onRehydrateStorage: () => (state) => {
        state?.markHydrated();
      },
    },
  ),
);
