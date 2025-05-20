import type { Task } from './task';

export type DeployTabName = '최근 빌드' | 'Https 세팅' | '빌드 기록';

export const DeployTabNames: DeployTabName[] = [
  '최근 빌드',
  'Https 세팅',
  '빌드 기록',
];

export interface DeployStatusProps {
  projectId: string;
  buildNumber: number | null;
  tasksByTab: Record<DeployTabName, Task[]>;
  selectedTab: DeployTabName;
  onTabChange: (tab: DeployTabName) => void;
  errorMessage: string;
}
