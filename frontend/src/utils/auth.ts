import { useUserStore } from '@/stores/userStore';

export async function fetchUserIfToken(): Promise<boolean> {
  const token = localStorage.getItem('accessToken');
  const refresh = localStorage.getItem('refreshToken');
  if (!token || !refresh) {
    return false;
  }

  try {
    const { fetchUser } = useUserStore.getState();
    await fetchUser();
    return true;
  } catch (e) {
    console.error('유저 정보 패칭 실패:', e);
    return false;
  }
}

export function clearUserData(): void {
  // 1) Zustand 스토어 초기화
  useUserStore.getState().clearUser();

  // 2) 로컬/세션 스토리지 정리
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  sessionStorage.removeItem('user');
}
