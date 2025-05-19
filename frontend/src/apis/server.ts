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
export async function convertServer(
  projectId: string,
  domain: string,
  email: string,
) {
  const payload = { projectId, domain, email };
  const res = await client.post('/server/convert', payload);
  return res.data;
}

/**
 * 서버에 EC2 세팅팅을 요청합니다.
 */
export async function startBuildWithPem(projectId: string, pemFile: File) {
  const formData = new FormData();
  formData.append('pemFile', pemFile);

  const res = await client.post('/server/deployment', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    params: { projectId },
  });

  return res.data;
}
