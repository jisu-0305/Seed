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
        {/* pwa */}
        <meta charSet="utf-8" />
        <link rel="manifest" href="/manifest.json" />

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
        {/* 라이트/다크 로고 둘 다 미리 로드 */}
        <link rel="preload" as="image" href="/assets/icons/ic_logo_light.svg" />
        <link rel="preload" as="image" href="/assets/icons/ic_logo_dark.svg" />
        <link rel="preload" as="image" href="/assets/icons/ic_logoText.svg" />
        <link
          rel="preload"
          as="image"
          href="/assets/icons/ic_logoText_white.svg"
        />
        <meta name="theme-color" content="#1c1c1c" />
      </head>
      <body>
        <MenuLayout>{children}</MenuLayout>
      </body>
    </html>
  );
}
