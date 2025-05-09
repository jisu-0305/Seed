import { client } from './axios';

export async function getUserRepos() {
  const { data } = await client.get('/gitlab/projects');
  return data;
}

export async function getDockerImage(image: string) {
  const { data } = await client.get(`/docker/images/${image}`);
  return data;
}

export async function getImageTag(image: string) {
  const { data } = await client.get(`/docker/images/${image}/tags`);
  return data;
}
