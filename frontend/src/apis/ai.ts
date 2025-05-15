// src/apis/ai/aiReportApi.ts
import { client } from '@/apis/axios';
import { AiReport, AiReportDetail } from '@/types/aiReport';

// projectId로 보고서 목록 조회
export async function getAiReports(projectId: string): Promise<AiReport[]> {
  const res = await client.get<{
    success: boolean;
    message: string;
    data: { reports: AiReport[] };
  }>(`/ai-report`, {
    params: { projectId },
  });
  return res.data.data.reports;
}

// reportId로 상세 정보 조회
export async function getAiReportDetail(
  reportId: number,
): Promise<AiReportDetail> {
  const res = await client.get<{
    success: boolean;
    message: string;
    data: AiReportDetail;
  }>(`/ai-report/detail`, {
    params: { reportId },
  });
  return res.data.data;
}
