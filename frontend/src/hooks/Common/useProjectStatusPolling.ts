import { useCallback, useEffect, useRef, useState } from 'react';

import { getProjectStatus } from '@/apis/project';

const STOP_STATUSES = [
  'INIT',
  'FINISH',
  'FAIL',
  'FINISH_WITH_AI',
  'FAIL_WTIH_AI',
  'BUILD_FAIL_WITH_AI',
];

export function useProjectStatusPolling(projectId: string) {
  const [status, setStatus] = useState<string | null>(null);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  // ✅ 내부적으로 polling 시작하는 함수
  const startPolling = useCallback(() => {
    if (intervalRef.current) clearInterval(intervalRef.current);

    const fetchAndUpdate = async () => {
      try {
        const result = await getProjectStatus(projectId);
        if (result) {
          setStatus(result.serverStatus);
          if (STOP_STATUSES.includes(result.serverStatus)) {
            clearInterval(intervalRef.current!);
            intervalRef.current = null;
            console.log('✅ 폴링 중단:', result.serverStatus);
          }
        } else {
          setStatus(null);
        }
      } catch (error) {
        console.error('서버 상태 가져오기 실패:', error);
      }
    };

    fetchAndUpdate(); // 최초 1회
    intervalRef.current = setInterval(fetchAndUpdate, 3000);
  }, [projectId]);

  useEffect(() => {
    startPolling();
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [startPolling]);

  // ✅ status와 polling 재시작 함수 반환
  return { status, restartPolling: startPolling };
}
