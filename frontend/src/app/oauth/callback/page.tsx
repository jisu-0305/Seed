'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';

export default function OAuthCallbackPage() {
  const router = useRouter();
  const params = useSearchParams();
  const token = params.get('token');
  const refresh = params.get('refresh');

  useEffect(() => {
    if (token && refresh) {
      localStorage.setItem('accessToken', token);
      localStorage.setItem('refreshToken', refresh);
      router.replace('/dashboard');
    }
  }, [token, refresh, router]);

  return <div>로그인 중입니다…</div>;
}
