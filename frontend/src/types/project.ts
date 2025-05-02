export interface StepStatus {
  gitlab: {
    repo: string;
    structure: string;
    directory: boolean;
  };
  server: {
    ip: string;
    pem: boolean;
  };
  app: string[];
  env: boolean;
}
