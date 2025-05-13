import { client } from './axios';

export async function getUserRepos(userId: number) {
  const { data } = await client.get(`/gitlab/users/${userId}/projects`);
  return data.data;
}

export async function getDockerImage(image: string) {
  const { data } = await client.get(`/docker/images/${image}`);
  return data;
}

export async function getImageTag(image: string) {
  const { data } = await client.get(`/docker/images/${image}/tags`);
  return data;
}
