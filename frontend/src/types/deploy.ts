import type { Task } from './task';

export type DeployTabName = '초기 세팅' | 'Https 세팅' | '빌드 기록';

export const DeployTabNames: DeployTabName[] = [
  '초기 세팅',
  'Https 세팅',
  '빌드 기록',
];

export interface DeployStatusProps {
  tasksByTab: Record<DeployTabName, Task[]>;
}
