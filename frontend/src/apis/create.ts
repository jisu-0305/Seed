import { useProjectFileStore } from '@/stores/projectStore';
import { PostProjectInfo } from '@/types/project';

import { client } from './axios';

export async function createProject(projectRequest: PostProjectInfo) {
  const { frontEnvFile, backEnvFile } = useProjectFileStore.getState();

  if (!frontEnvFile || !backEnvFile) {
    throw new Error('모든 파일이 업로드되어야 합니다.');
  }

  const formData = new FormData();
  formData.append(
    'projectRequest',
    new Blob([JSON.stringify(projectRequest)], { type: 'application/json' }),
  );
  formData.append('clientEnvFile', frontEnvFile);
  formData.append('serverEnvFile', backEnvFile);

  const res = await client.post('/projects', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return res.data;
}
