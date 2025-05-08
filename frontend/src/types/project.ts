export interface ProjectInfo {
  gitlab: {
    repo: string;
    structure: '모노' | '멀티' | string;
    directory: {
      client: string;
      server: string;
    };
  };
  server: {
    ip: string;
    pem: boolean;
  };
  app: {
    name: string;
    tag: string;
    port: number;
  }[];
  env: {
    env: boolean;
    node: string;
    jdk: number;
    buildTool: string;
  };
}

export type BuildStatus = 'SUCCESS' | 'FAILURE' | null;

export interface ProjectSummary {
  id: number;
  projectName: string;
  createdAt: string;
  memberList: ProjectMember[];
  autoDeploymentEnabled: boolean;
  httpsEnabled: boolean;
  buildStatus: BuildStatus;
  lastBuildAt: string;
}

export interface ProjectMember {
  userId: number;
  userName: string;
  userIdentifyId: string;
  profileImageUrl: string;
}
