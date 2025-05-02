'use client';

import { Suspense } from 'react';

import OAuthCallback from '@/components/Landing/oauth';

export default function OAuthCallbackPage() {
  return (
    <Suspense>
      <OAuthCallback />
    </Suspense>
  );
}

export const dynamic = 'force-dynamic';
