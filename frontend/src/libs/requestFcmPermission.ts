import { registerFcmToken } from '@/apis/fcm';

// libs/requestFcmPermission.ts
export async function requestFcmPermission(userId: number) {
  if (typeof window === 'undefined') return;

  const { isSupported, getMessaging, getToken } = await import(
    'firebase/messaging'
  );

  const supported = await isSupported();
  if (!supported) return;

  try {
    const messaging = getMessaging();

    const permission = await Notification.requestPermission();
    if (permission !== 'granted') {
      console.warn('🔕 알림 권한 거부됨');
      return;
    }

    const token = await getToken(messaging, {
      vapidKey: process.env.NEXT_PUBLIC_VAPID_KEY!,
      serviceWorkerRegistration: await navigator.serviceWorker.ready,
    });

    if (!token) throw new Error('FCM 토큰 발급 실패');

    await registerFcmToken(userId, token);
    console.log('✅ FCM 토큰 등록 완료:', token);
  } catch (err) {
    console.error('❌ FCM 요청 실패:', err);
  }
}
