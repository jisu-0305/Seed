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
  pemFile: File,
) {
  if (!projectId || !domain || !email || !pemFile) {
    throw new Error(
      '필수 값이 누락되었습니다. projectId, domain, email, pemFile 모두 필요합니다.',
    );
  }

  const formData = new FormData();

  // JSON 객체를 Blob으로 만들어 request 필드에 첨부
  const requestPayload = {
    projectId: Number(projectId),
    domain,
    email,
  };
  const jsonBlob = new Blob([JSON.stringify(requestPayload)], {
    type: 'application/json',
  });

  formData.append('request', jsonBlob);
  formData.append('pemFile', pemFile);

  const res = await client.post('/server/convert', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

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
