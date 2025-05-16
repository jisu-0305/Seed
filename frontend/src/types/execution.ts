export interface Execution {
  id: number;
  projectName: string;
  executionType: 'BUILD' | 'DEPLOY' | 'HTTPS' | string;
  projectExecutionTitle: string;
  executionStatus: 'SUCCESS' | 'FAIL' | string;
  buildNumber: string;
  createdAt: string; // "YYYY-MM-DD" 혹은 ISO
}

export interface ExecutionsByDate {
  date: string; // "2025-05-16"
  executionList: Execution[];
}

export interface ExecutionsResponse {
  success: boolean;
  message: string;
  data: ExecutionsByDate[];
}
