import { client } from './axios';

export async function getUserRepos() {
  const { data } = await client.get('/gitlab/projects');
  return data;
}
