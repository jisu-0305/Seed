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
  lastBuildAt: string | null;
}

export interface ProjectMember {
  userId: number;
  userName: string;
  userIdentifyId: string;
  profileImageUrl: string;
  status: 'accepted' | 'pending' | 'rejected';
}

export interface Application {
  imageName: string;
  tag: string;
  port: number;
}

export interface ProjectDetailData {
  id: number;
  ownerId: number;
  projectName: string;
  createdAt: string;
  serverIP: string;
  repositoryUrl: string;
  structure: 'MONO' | string;
  frontendDirectoryName: string;
  backendDirectoryName: string;
  frontendBranchName: string;
  backendBranchName: string;
  frontendFramework: string;
  frontendEnvFilePath: string;
  nodejsVersion: string;
  jdkVersion: string;
  jdkBuildTool: string;
  backendEnvFilePath: string;
  applicationList: Application[];
  pemFilePath: string;
}

export interface ProjectDetailResponse {
  success: boolean;
  message: string;
  data: ProjectDetailData;
}
