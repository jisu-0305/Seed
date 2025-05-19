import { client } from './axios';

export async function getUserRepos(userId: number) {
  const { data } = await client.get(`/gitlab/users/${userId}/projects`);
  return data.data;
}

export async function getUserReposCursor(userId: number, lastId?: number) {
  console.log('레포 조회');

  const url = lastId
    ? `/gitlab/users/${userId}/projects/cursor?lastId=${lastId}`
    : `/gitlab/users/${userId}/projects/cursor`;

  const { data } = await client.get(url);
  return data.data.projects;
}

export async function getDockerImage(image: string) {
  const { data } = await client.get(`/docker/images/${image}`);
  return data;
}

export async function getProjectApplications(keyword: string) {
  const { data } = await client.get('/projects/applications', {
    params: { keyword },
  });
  return data;
}

export async function getImageTag(image: string) {
  const { data } = await client.get(`/docker/images/${image}/tags`);
  return data;
}
