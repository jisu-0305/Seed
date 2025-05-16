// 빌드 상태
export type TaskStatus = 'SUCCESS' | 'FAIL' | 'FAILED' | 'IN PROGRESS';

export interface Task {
  stepNumber: number;
  stepName: string;
  duration: string;
  status: TaskStatus;
  echoList?: EchoList[];
}

export interface EchoList {
  echoNumber: number;
  echoContent: string;
  duration: string;
}
