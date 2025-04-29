import { ReactNode } from 'react';

import { MenuLayout } from './MenuLayout';

export const metadata = {
  title: 'SEED',
  description: 'Super E Easy Deployment',
  icons: { icon: '/favicon.ico' },
};

export default function RootLayout({ children }: { children: ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <MenuLayout>{children}</MenuLayout>
      </body>
    </html>
  );
}
