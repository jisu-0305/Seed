// components/FCMButton.tsx

'use client';

import { useEffect } from 'react';

import { initFcmForegroundListener } from '@/libs/fcmListener';
import { requestFcmPermission } from '@/libs/requestFcmPermission';
import { useUserStore } from '@/stores/userStore';

import SmallButton from './button/SmallButton';

export default function FCMButton() {
  const user = useUserStore((s) => s.user);

  useEffect(() => {
    // 서비스 워커 등록
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker
        .register('sw.js')
        .then((reg) => console.log('SW 등록:', reg))
        .catch((err) => console.error('SW 등록 실패:', err));
    }

    initFcmForegroundListener();
  }, []);

  const handleClick = async () => {
    if (!user?.userId) return;
    requestFcmPermission(user.userId);
  };

  return (
    <div>
      <SmallButton onClick={handleClick}>🔑 FCM 토큰 가져오기</SmallButton>
    </div>
  );
}
