import '@emotion/react';

import { lightTheme } from './theme';

// type ThemeType = typeof theme;
type ThemeType = typeof lightTheme;

declare module '@emotion/react' {
  export interface Theme extends ThemeType {}
}
