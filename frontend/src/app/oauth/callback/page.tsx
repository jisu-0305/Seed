'use client';

import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';

import { useUserStore } from '@/stores/userStore';

export default function OAuthCallbackPage() {
  const router = useRouter();
  const params = useSearchParams();
  const token = params.get('token');
  const refresh = params.get('refresh');
  const fetchUser = useUserStore((s) => s.fetchUser);

  useEffect(() => {
    async function handleLogin() {
      if (token && refresh) {
        // 1) 토큰 저장
        localStorage.setItem('accessToken', token);
        localStorage.setItem('refreshToken', refresh);

        // 2) 클라이언트 상태에 유저 정보 패칭
        await fetchUser();

        // 3) 대시보드로 이동
        router.replace('/dashboard');
      } else {
        // 토큰 파라미터가 없으면 로그인 페이지로
        router.replace('/login');
      }
    }

    handleLogin();
  }, [token, refresh, fetchUser, router]);

  return <div>로그인 중입니다…</div>;
}
