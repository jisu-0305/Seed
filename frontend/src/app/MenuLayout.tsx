'use client';

import { Global, ThemeProvider } from '@emotion/react';
import styled from '@emotion/styled';
import { QueryClientProvider } from '@tanstack/react-query';
import { usePathname } from 'next/navigation';
import React, { ReactNode, useEffect } from 'react';

import SideBar from '@/components/Common/SideBar';
import { queryClient } from '@/libs/queryClient';
import { useThemeStore } from '@/stores/themeStore';
import { globalStyles } from '@/styles/global';
import { darkTheme, lightTheme } from '@/styles/theme';

export function MenuLayout({ children }: { children: ReactNode }) {
  const pathName = usePathname();
  const { mode, setMode, hasHydrated } = useThemeStore();

  useEffect(() => {
    if (hasHydrated && mode === null) {
      const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
      setMode(isDark ? 'dark' : 'light');
    }
  }, [hasHydrated, mode, setMode]);

  if (!hasHydrated || mode === null) {
    return null;
  }

  return (
    // <ThemeProvider theme={theme}>
    <ThemeProvider theme={mode === 'dark' ? darkTheme : lightTheme}>
      <QueryClientProvider client={queryClient}>
        <Global
          styles={globalStyles(mode === 'dark' ? darkTheme : lightTheme)}
        />
        <LayoutWrapper>
          {pathName !== '/' &&
            pathName !== '/login' &&
            pathName !== '/oauth/callback' &&
            pathName !== '/onboarding' && <SideBar />}
          <RightArea>
            <ContentWrapper>{children}</ContentWrapper>
          </RightArea>
        </LayoutWrapper>
      </QueryClientProvider>
    </ThemeProvider>
  );
}

const LayoutWrapper = styled.div`
  display: flex;
  width: 100%;
  height: 100%;
`;

const RightArea = styled.div`
  display: flex;
  flex-direction: column;
  flex-grow: 1;
  height: 100%;
`;

const ContentWrapper = styled.main`
  flex-grow: 1;
`;
