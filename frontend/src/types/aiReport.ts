// src/types/aiReport.ts
export interface AiReport {
  id: number;
  title: string;
  status: 'SUCCESS' | 'FAIL' | 'REJECTED';
  date: string;
}

export interface AiReportDetail {
  title: string;
  summary: string;
  files: string[];
  status: 'SUCCESS' | 'FAIL' | 'REJECTED';
  detail: string;
  commitUrl: string;
  mergeRequestUrl: string;
}
