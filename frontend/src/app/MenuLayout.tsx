'use client';

import { Global, ThemeProvider } from '@emotion/react';
import styled from '@emotion/styled';
import { QueryClientProvider } from '@tanstack/react-query';
import { usePathname } from 'next/navigation';
import React, { ReactNode } from 'react';

import SideBar from '@/components/Common/SideBar';
import { queryClient } from '@/libs/queryClient';
import { globalStyles } from '@/styles/global';
import { theme } from '@/styles/theme';

export function MenuLayout({ children }: { children: ReactNode }) {
  const pathName = usePathname();

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
`;
