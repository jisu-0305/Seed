'use client';

import { Global, ThemeProvider } from '@emotion/react';
import styled from '@emotion/styled';
import { QueryClientProvider } from '@tanstack/react-query';
import { usePathname } from 'next/navigation';
import React, { ReactNode, useEffect } from 'react';

import SideBar from '@/components/Common/SideBar';
import { queryClient } from '@/libs/queryClient';
import { useUserStore } from '@/stores/userStore';
import { globalStyles } from '@/styles/global';
import { theme } from '@/styles/theme';

export function MenuLayout({ children }: { children: ReactNode }) {
  const pathName = usePathname();
  const user = useUserStore((s) => s.user);
  const loading = useUserStore((s) => s.loading);
  const fetchUser = useUserStore((s) => s.fetchUser);
  const hasHydrated = useUserStore((s) => s.hasHydrated);

  useEffect(() => {
    if (hasHydrated && !user && !loading) {
      fetchUser();
    }
  }, [hasHydrated, user, loading, fetchUser]);

  return (
    <ThemeProvider theme={theme}>
      <QueryClientProvider client={queryClient}>
        <Global styles={globalStyles(theme)} />
        <LayoutWrapper>
          {pathName !== '/' &&
            pathName !== '/login' &&
            pathName !== '/oauth/callback' && <SideBar />}
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
  overflow-y: auto;
`;
