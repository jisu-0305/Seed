// public/firebase-messaging-sw.js

// 1. Workbox precaching imports
import { precacheAndRoute, cleanupOutdatedCaches } from 'workbox-precaching';

// 2. This is the placeholder that next-pwa (injectManifest) will replace
precacheAndRoute(self.__WB_MANIFEST);

// 3. (Optional) clean up old caches automatically
cleanupOutdatedCaches();

importScripts(
  'https://www.gstatic.com/firebasejs/10.10.0/firebase-app-compat.js',
);
importScripts(
  'https://www.gstatic.com/firebasejs/10.10.0/firebase-messaging-compat.js',
);

firebase.initializeApp({
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID,
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  console.log('📥 백그라운드 메시지 수신됨:', payload);
  const { title, body } = payload.notification || payload.data || {};
  self.registration.showNotification(title || '알림', {
    body: body || '내용 없음',
  });
});
