'use client';

import { Global, ThemeProvider } from '@emotion/react';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactNode } from 'react';

import { queryClient } from '@/libs/queryClient';
import { globalStyles } from '@/styles/global';
import { theme } from '@/styles/theme';

export default function Providers({ children }: { children: ReactNode }) {
  return (
    <ThemeProvider theme={theme}>
      <QueryClientProvider client={queryClient}>
        <Global styles={globalStyles(theme)} />
        {children}
      </QueryClientProvider>
    </ThemeProvider>
  );
}
