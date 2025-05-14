import { useRouter, useSearchParams } from 'next/navigation';
import { useEffect } from 'react';

import { useUserStore } from '@/stores/userStore';
import { fetchUserIfToken } from '@/utils/auth';

export default function OAuthCallback() {
  const router = useRouter();
  const params = useSearchParams();
  const token = params.get('token');
  const refresh = params.get('refresh');

  useEffect(() => {
    (async () => {
      if (!token || !refresh) {
        router.replace('/login');
        return;
      }

      localStorage.setItem('accessToken', token);
      localStorage.setItem('refreshToken', refresh);

      const ok = await fetchUserIfToken();
      const { user } = useUserStore.getState();

      if (!ok || !user) {
        router.replace('/login');
        return;
      }

      if (user.hasGitlabPersonalAccessToken) {
        router.replace('/dashboard');
      } else {
        router.replace('/onboarding');
      }
    })();
  }, [token, refresh, router]);

  return <div>로그인 중입니다…</div>;
}
