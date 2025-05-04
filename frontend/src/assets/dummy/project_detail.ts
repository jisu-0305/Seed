// src/assets/dummy/project_detail.ts
import type { DeployTabName } from '@/types/deploy';
import type { Task } from '@/types/task';

export const project = {
  id: 1,
  emojiSrc: '/assets/projectcard/project_default.png',
  projectName: 'K‑ing Service',
  httpsEnabled: true,
  autoDeployEnabled: true,
  lastBuildStatus: 'SUCCESS' as const,
  lastBuildTime: '05.01 23:32:10',
  users: [
    { id: 101, name: '김예슬', avatarUrl: '/assets/user.png' },
    { id: 102, name: '이준호', avatarUrl: '/assets/user.png' },
    { id: 103, name: '박지민', avatarUrl: '/assets/user.png' },
  ],
  projectInfo: {
    clientDir: 'frontend',
    serverDir: 'backend',
    nodeVersion: 'v22.14.0',
    jdkVersion: '17',
    buildTool: 'Gradle',
  },
};

export const tasksByTab = {
  '초기 세팅': [
    {
      no: 1,
      description: 'EC2 배포를 위한 설정',
      duration: '3hr 20min',
      status: 'Complete',
    },
    {
      no: 2,
      description: '애플리케이션 환경 설정',
      duration: '2hr 10min',
      status: 'Complete',
    },
    {
      no: 3,
      description: 'docker-compose 설정',
      duration: '1hr 45min',
      status: 'Complete',
    },
    { no: 4, description: 'git clone', duration: '10min', status: 'Complete' },
    {
      no: 5,
      description: '환경 변수 설정',
      duration: '15min',
      status: 'Complete',
    },
    { no: 6, description: 'Nginx, HTTP 설정', duration: '-', status: 'Fail' },
  ],
  'Https 세팅': [
    {
      no: 1,
      description: 'SSL 인증서 발급',
      duration: '1hr 15min',
      status: 'Complete',
    },
    {
      no: 2,
      description: 'nginx.conf 수정',
      duration: '45min',
      status: 'Complete',
    },
    {
      no: 3,
      description: '방화벽 포트 개방',
      duration: '10min',
      status: 'Complete',
    },
    {
      no: 4,
      description: '도메인 DNS 설정',
      duration: '-',
      status: 'In Progress',
    },
    {
      no: 5,
      description: 'HTTPS 리다이렉트 테스트',
      duration: '-',
      status: 'In Progress',
    },
  ],
  '빌드 기록': [
    {
      no: 1,
      description: '#132 MR 빌드',
      duration: '00:45:12',
      status: 'Complete',
    },
    {
      no: 2,
      description: '#133 MR 빌드',
      duration: '01:12:34',
      status: 'Fail',
    },
    {
      no: 3,
      description: '#134 MR 빌드',
      duration: '00:30:05',
      status: 'Complete',
    },
    {
      no: 4,
      description: '#135 MR 빌드',
      duration: '-',
      status: 'In Progress',
    },
    {
      no: 5,
      description: '#136 MR 빌드',
      duration: '-',
      status: 'In Progress',
    },
  ],
} satisfies Record<DeployTabName, Task[]>;
