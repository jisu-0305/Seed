/* eslint-disable @next/next/no-page-custom-font */
import { MenuLayout } from './MenuLayout';

export const metadata = {
  title: 'SEED',
  description: 'Super E Easy Deployment',
  icons: { icon: '/favicon.ico' },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ko">
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link
          rel="preconnect"
          href="https://fonts.gstatic.com"
          crossOrigin="anonymous"
        />
        <link
          href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap"
          rel="stylesheet"
        />
      </head>
      <body>
        <MenuLayout>{children}</MenuLayout>
      </body>
    </html>
  );
}
