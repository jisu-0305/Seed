// components/FCMButton.tsx

'use client';

import { getToken, onMessage } from 'firebase/messaging';
import { useEffect, useState } from 'react';

import { registerFcmToken } from '@/apis/fcm';
import { messaging } from '@/libs/firebaseClient';
import { useUserStore } from '@/stores/userStore';

import SmallButton from './button/SmallButton';

export default function FCMButton() {
  const [token, setToken] = useState<string | null>(null);
  const user = useUserStore((s) => s.user);

  useEffect(() => {
    // 서비스 워커 등록
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker
        .register('sw.js')
        .then((reg) => console.log('SW 등록:', reg))
        .catch((err) => console.error('SW 등록 실패:', err));
    }

    // 포그라운드 메시지 처리 (TTS + alert)
    onMessage(messaging, (payload) => {
      const title = payload.notification?.title ?? payload.data?.title;
      const body = payload.notification?.body ?? payload.data?.body;
      if (!title && !body) return;

      // TTS
      const utterance = new SpeechSynthesisUtterance(`${title}: ${body}`);
      utterance.lang = 'ko-KR';
      window.speechSynthesis.speak(utterance);

      // 알림 UI
      alert(`${title}\n${body}`);
    });
  }, []);

  const handleClick = async () => {
    if (!user?.userId) return;
    try {
      const permission = await Notification.requestPermission();
      if (permission !== 'granted') throw new Error('알림 권한 거부됨');

      const currentToken = await getToken(messaging, {
        vapidKey: process.env.NEXT_PUBLIC_VAPID_KEY!,
        serviceWorkerRegistration: await navigator.serviceWorker.ready,
      });
      if (!currentToken) throw new Error('FCM 토큰 발급 실패');

      setToken(currentToken);
      // 여기서 API 호출
      await registerFcmToken(user.userId, currentToken);
      console.log('FCM 토큰 등록 완료');
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div>
      <SmallButton onClick={handleClick}>🔑 FCM 토큰 가져오기</SmallButton>
      {token && <pre style={{ wordBreak: 'break-all' }}>{token}</pre>}
    </div>
  );
}
