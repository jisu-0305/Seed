import '@emotion/react';

import { theme } from './theme';

type ThemeType = typeof theme;

declare module '@emotion/react' {
  // export interface Theme extends ThemeType {}

  export interface Theme {
    mode: 'light' | 'dark';
    colors: Record<string, string>;
    fonts: Record<string, string>;
  }
}
