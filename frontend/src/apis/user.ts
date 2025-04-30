import { MeResponse } from '@/types/user';

import { client } from './axios';

export async function fetchMe(): Promise<MeResponse> {
  const res = await client.get<MeResponse>('/users/me');
  return res.data;
}

export async function logout(): Promise<void> {
  await client.post('/users/logout');
}
