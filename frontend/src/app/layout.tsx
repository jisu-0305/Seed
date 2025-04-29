// layout은 해당 경로 이하에 공통으로 적용되는 레이아웃

import { ReactNode } from 'react';

import Providers from './Providers';

export const metadata = {
  title: 'SEED',
  description: 'Super E Easy Deployment',
  icons: {
    icon: '/favicon.ico',
  },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
