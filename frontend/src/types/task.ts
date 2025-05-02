// 빌드 상태
export type TaskStatus = 'Complete' | 'Fail' | 'In Progress' | '-';

export interface Task {
  no: number;
  description: string;
  duration: string;
  status: TaskStatus;
}
