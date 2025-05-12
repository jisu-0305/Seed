import { client } from './axios';

export interface HttpsSetupRequest {
  pid: number;
  domain: string;
  email: string;
}

export interface ConvertServerPayload {
  request: HttpsSetupRequest;
  pemFile: string;
}

/**
 * 서버에 HTTPS 설정 변환을 요청합니다.
 */
export async function convertServer(
  payload: ConvertServerPayload,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): Promise<any> {
  const res = await client.post('/server/convert', payload);
  return res.data;
}
