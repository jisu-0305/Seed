'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';

import { fetchUserIfToken } from '@/utils/auth';

export default function OAuthCallbackPage() {
  const router = useRouter();
  const params = useSearchParams();
  const token = params.get('token');
  const refresh = params.get('refresh');

  useEffect(() => {
    (async () => {
      if (token && refresh) {
        localStorage.setItem('accessToken', token);
        localStorage.setItem('refreshToken', refresh);

        const ok = await fetchUserIfToken();
        if (ok) {
          router.replace('/dashboard');
        } else {
          router.replace('/login');
        }
      } else {
        router.replace('/login');
      }
    })();
  }, [token, refresh, router]);

  return <div>로그인 중입니다…</div>;
}
