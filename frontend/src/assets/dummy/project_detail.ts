// src/assets/dummy/project_detail.ts

export const project = {
  id: 1,
  emojiSrc: '/assets/projectcard/project_default.png',
  projectName: 'K‑ing Service',
  httpsEnabled: true,
  autoDeployEnabled: true,
  lastBuildStatus: 'SUCCESS' as const,
  lastBuildTime: '05.01 23:32:10',
  users: [
    { id: 101, name: '김예슬', profileImageUrl: '/assets/user.png' },
    { id: 102, name: '이준호', profileImageUrl: '/assets/user.png' },
    { id: 103, name: '박지민', profileImageUrl: '/assets/user.png' },
  ],
  projectInfo: {
    folder: 'Mono',
    clientDir: 'frontend',
    serverDir: 'backend',
    nodeVersion: 'v22.14.0',
    jdkVersion: '17',
    buildTool: 'Gradle',
  },
};
