import { client } from './axios';

export interface HttpsSetupRequest {
  projectId: string;
  domain: string;
  email: string;
}

export interface ConvertServerPayload {
  request: HttpsSetupRequest;
  pemFilePath: string;
}

/**
 * 서버에 HTTPS 설정 변환을 요청합니다.
 */
// src/apis/server.ts

export async function convertServer(
  projectId: string,
  domain: string,
  email: string,
  pemFilePath: string,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
): Promise<any> {
  // 1) FormData 생성
  const formData = new FormData();
  // 2) JSON 파트(request) 추가: Blob 으로 감싸기
  formData.append(
    'request',
    new Blob([JSON.stringify({ projectId, domain, email })], {
      type: 'application/json',
    }),
  );
  // 3) pemFilePath 문자열 파트 추가
  formData.append('pemFilePath', pemFilePath);
  //    → 파일 객체를 업로드하려면 .append('pemFile', File) 형태로 바꾸면 됩니다.

  // 4) axios POST 요청
  const res = await client.post('/server/convert', formData, {
    // axios 가 자동으로 multipart/form-data; boundary=… 를 세팅해 줍니다.
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return res.data;
}
