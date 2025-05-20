import { useCallback, useEffect, useRef, useState } from 'react';

import { getProjectStatus } from '@/apis/project';
import { SERVER_STATUS_INFO } from '@/utils/getStatusMessage';

const STOP_STATUSES = [
  'INIT',
  'FINISH',
  'FAIL',
  'FINISH_WITH_AI',
  'FAIL_WTIH_AI',
  'BUILD_FAIL_WITH_AI',
  'FINISH_CONVERT_HTTPS',
];

const BUILD_STATUSES = ['INIT', 'FINISH', 'FAIL'];

export function useProjectStatusPolling(projectId: string) {
  const [status, setStatus] = useState<string | null>(null);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  const fetchAndUpdate = useCallback(async () => {
    try {
      const result = await getProjectStatus(projectId);
      if (result) {
        setStatus(result.serverStatus);
        if (STOP_STATUSES.includes(result.serverStatus)) {
          clearInterval(intervalRef.current!);
          intervalRef.current = null;
          console.log('상태 중단:', result.serverStatus);
        }
      } else {
        setStatus(null);
      }
    } catch (error) {
      console.error('서버 상태 가져오기 실패:', error);
    }
  }, [projectId]);

  const startPolling = useCallback(() => {
    if (intervalRef.current) clearInterval(intervalRef.current);
    fetchAndUpdate(); // 최초 1회 실행
    intervalRef.current = setInterval(fetchAndUpdate, 3000);
  }, [fetchAndUpdate]);

  const stopPolling = useCallback(() => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
      timeoutRef.current = null;
    }
  }, []);

  // ✅ 10초간만 polling 수행하는 함수
  const pollForSeconds = useCallback(
    (seconds: number = 10) => {
      stopPolling(); // 기존 polling 중단
      startPolling(); // 새로 시작
      timeoutRef.current = setTimeout(() => {
        stopPolling(); // 10초 후 polling 종료
        console.log('⏱️ 10초간 polling 완료');
      }, seconds * 1000);
    },
    [startPolling, stopPolling],
  );

  useEffect(() => {
    startPolling();
    return () => stopPolling();
  }, [startPolling, stopPolling]);

  // ✅ 현재 빌드 로딩 여부: build 카테고리면서 STOP 상태가 아닌 경우
  const statusInfo = SERVER_STATUS_INFO[status ?? ''];
  const isBuildLoading =
    statusInfo?.category === 'build' && !BUILD_STATUSES.includes(status ?? '');

  // ✅ HTTPS 로딩: https 카테고리 + FINISH_CONVERT_HTTPS 이전
  const isHttpsLoading =
    statusInfo?.category === 'https' && status !== 'FINISH_CONVERT_HTTPS';

  // ✅ status와 polling 재시작 함수 반환
  return {
    status,
    isBuildLoading,
    isHttpsLoading,
    restartPolling: startPolling,
    pollForSeconds,
  };
}
