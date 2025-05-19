// libs/fcmListener.ts
import { onMessage } from 'firebase/messaging';

import { messaging } from './firebaseClient';

export function initFcmForegroundListener() {
  onMessage(messaging, (payload) => {
    const title = payload.notification?.title ?? payload.data?.title;
    const body = payload.notification?.body ?? payload.data?.body;
    if (!title && !body) return;

    // tts
    // const utterance = new SpeechSynthesisUtterance(`${title}: ${body}`);
    // utterance.lang = 'ko-KR';
    // window.speechSynthesis.speak(utterance);

    alert(`${title}\n${body}`);
  });
}
