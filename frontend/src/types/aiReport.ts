// src/types/aiReport.ts
export interface AiReport {
  id: string;
  title: string;
  date: string;
  status: 'In Progress' | 'Merged' | 'Rejected';
  summary: string;
  files: string[];
  detail: string;
}
