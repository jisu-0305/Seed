import { useEffect, useState } from 'react';

export type ThemeMode = 'light' | 'dark';

const THEME_KEY = 'theme-mode';

export const useOSMode = () => {
  const [mode, setMode] = useState<ThemeMode>('light');

  // 초기 설정 (OS 기반 or localStorage)
  useEffect(() => {
    const stored = localStorage.getItem(THEME_KEY) as ThemeMode | null;
    if (stored === 'light' || stored === 'dark') {
      setMode(stored);
    } else {
      const systemDark = window.matchMedia(
        '(prefers-color-scheme: dark)',
      ).matches;
      const defaultMode = systemDark ? 'dark' : 'light';
      setMode(defaultMode);
      localStorage.setItem(THEME_KEY, defaultMode);
    }
  }, []);

  // 수동 변경
  const toggleMode = () => {
    const newMode = mode === 'light' ? 'dark' : 'light';
    setMode(newMode);
    localStorage.setItem(THEME_KEY, newMode);
  };

  return { mode, toggleMode };
};
