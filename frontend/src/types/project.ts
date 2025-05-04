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
    version: string;
    port: number;
  }[];
  env: {
    env: boolean;
    node: string;
    jdk: number;
    buildTool: string;
  };
}
