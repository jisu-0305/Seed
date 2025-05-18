// src/apis/fcm.ts
import { client } from './axios';

/**
 * FCM 토큰을 서버에 등록합니다.
 * @param userId  로그인한 유저의 ID
 * @param token   getToken() 으로 발급받은 FCM 토큰
 */
export async function registerFcmToken(
  userId: number,
  token: string,
): Promise<void> {
  // POST /api/fcm-tokens?userId=123&token=abcd
  await client.post<void>('/fcm-tokens', null, {
    params: { userId, token },
  });
}
