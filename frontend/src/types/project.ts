export interface ProjectInfo {
  gitlab: {
    id: number;
    repo: string;
    defaultBranch: string;
    structure: '모노' | '멀티' | string;
    directory: {
      client: string;
      server: string;
    };
  };
  server: {
    ip: string;
    pem: boolean;
    pemName: string;
  };
  app: Application[];
  env: {
    frontendFramework: string;
    frontEnv: boolean;
    frontEnvName: string;
    backEnv: boolean;
    backEnvName: string;
    node: string;
    jdk: string;
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
  status: 'ACCEPTED' | 'PENDING' | 'OWNER' | 'UNKNOWN';
}

export interface Application {
  imageName: string;
  tag: string;
  port: number;
}

// defaultPorts 가 필요한 곳에서만 쓰는 서브타입
export interface ApplicationWithDefaults extends Application {
  defaultPorts: number[];
  description: string;
  imageEnvs: string[];
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
  domainName: string;
  applicationList: Application[];
  pemFilePath: string;
}

export interface ProjectDetailResponse {
  success: boolean;
  message: string;
  data: ProjectDetailData;
}

interface BaseProjectInfo {
  gitlabProjectId: number;
  repositoryUrl: string;
  gitlabTargetBranch: string;
  jdkVersion: string;
  serverIP: string;
  frontendFramework: string;
  nodejsVersion: string;
  applicationList: Application[];
  jdkBuildTool: string;
}

export interface PostMonoProjectInfo extends BaseProjectInfo {
  structure: 'MONO';
  backendDirectoryName: string;
  frontendDirectoryName: string;
}

export interface PostMultiProjectInfo extends BaseProjectInfo {
  structure: 'MULTI';
  backendBranchName: string;
  frontendBranchName: string;
}

export type PostProjectInfo = PostMonoProjectInfo | PostMultiProjectInfo;

// 대시보드 프로젝트 카드
export interface ProjectCardInfo {
  id: number;
  projectName: string;
  httpsEnabled: boolean;
  autoDeploymentEnabled: boolean;
  buildStatus: BuildStatus;
  lastBuildAt: string;
  createdAt: string;
}

// 프로젝트 수정할 때
export interface ProjectUpdateRequest {
  serverIP: string;
  applications: {
    imageName: string;
    tag: string;
    port: number;
  }[];
}

// —————— 서브타입 정의 ——————
export interface GitlabInfo {
  id: number;
  repo: string;
  defaultBranch: string;
  structure: '모노' | '멀티' | string;
  directory: {
    client: string;
    server: string;
  };
}

export interface ServerInfo {
  ip: string;
  pem: boolean;
  pemName: string;
}

export interface EnvInfo {
  frontendFramework: string;
  frontEnv: boolean;
  frontEnvName: string;
  backEnv: boolean;
  backEnvName: string;
  node: string;
  jdk: string;
  buildTool: string;
}
