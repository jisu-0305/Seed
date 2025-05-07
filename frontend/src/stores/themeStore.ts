import { create } from 'zustand';
import { persist } from 'zustand/middleware';

type ThemeMode = 'light' | 'dark' | null;

interface ThemeStore {
  mode: ThemeMode;
  setMode: (mode: ThemeMode) => void;
  toggleMode: () => void;
  hasHydrated: boolean;
  setHasHydrated: (value: boolean) => void;
}

export const useThemeStore = create<ThemeStore>()(
  persist(
    (set, get) => ({
      mode: null,
      hasHydrated: false,
      setMode: (mode) => set({ mode }),
      toggleMode: () =>
        set({ mode: get().mode === 'light' ? 'dark' : 'light' }),
      setHasHydrated: (value) => set({ hasHydrated: value }),
    }),
    {
      name: 'theme-mode',
      onRehydrateStorage: () => (state) => {
        state?.setHasHydrated(true);
      },
    },
  ),
);
