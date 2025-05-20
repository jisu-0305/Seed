import { useCallback, useEffect, useMemo, useRef, useState } from 'react';

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

interface UsePollingOptions {
  onBuildFinish?: () => void;
  onHttpsFinish?: () => void;
}

const BUILD_STATUSES = ['INIT', 'FINISH', 'FAIL', 'FINISH_CONVERT_HTTPS'];

export function useProjectStatusPolling(
  projectId: string,
  options?: UsePollingOptions,
) {
  const { onBuildFinish, onHttpsFinish } = options || {};

  const [status, setStatus] = useState<string | null>(null);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  const buildFinishCalledRef = useRef(false);
  const httpsFinishCalledRef = useRef(false);

  const fetchAndUpdate = useCallback(
    async (force = false) => {
      try {
        const result = await getProjectStatus(projectId);

        if (result) {
          const nextStatus = result.serverStatus;
          console.log('서버 상태:', nextStatus);
          setStatus(nextStatus);
          const statusInfo = SERVER_STATUS_INFO[nextStatus];

          // ✅ build 완료 콜백 (한 번만 호출)
          if (
            statusInfo?.category === 'build' &&
            nextStatus === 'FINISH' &&
            !buildFinishCalledRef.current
          ) {
            onBuildFinish?.();
            buildFinishCalledRef.current = true;
          }

          // ✅ https 완료 콜백 (한 번만 호출)
          if (
            statusInfo?.category === 'https' &&
            nextStatus === 'FINISH_CONVERT_HTTPS' &&
            !httpsFinishCalledRef.current
          ) {
            onHttpsFinish?.();
            httpsFinishCalledRef.current = true;
          }

          // ✅ force가 아닌 경우에만 stop 조건 체크
          if (!force && STOP_STATUSES.includes(nextStatus)) {
            clearInterval(intervalRef.current!);
            intervalRef.current = null;
          }
        } else {
          setStatus(null);
        }
      } catch (error) {
        console.error('서버 상태 가져오기 실패:', error);
      }
    },
    [projectId],
  );

  const startPolling = useCallback(
    (force = false) => {
      if (intervalRef.current) clearInterval(intervalRef.current);
      fetchAndUpdate(force);
      intervalRef.current = setInterval(() => {
        fetchAndUpdate(force); // ✅ 반복 polling에도 동일한 force 사용
      }, 5000);
    },
    [fetchAndUpdate],
  );

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

  useEffect(() => {
    startPolling();
    return () => stopPolling();
  }, [startPolling, stopPolling]);

  // ✅ 현재 빌드 로딩 여부: build 카테고리면서 STOP 상태가 아닌 경우
  const statusInfo = useMemo(() => SERVER_STATUS_INFO[status ?? ''], [status]);

  const isBuildLoading = useMemo(() => {
    if (!status || !statusInfo) return false;
    return statusInfo.category === 'build' && !BUILD_STATUSES.includes(status);
  }, [status, statusInfo]);

  const isHttpsLoading = useMemo(() => {
    if (!status || !statusInfo) return false;
    return statusInfo.category === 'https' && status !== 'FINISH_CONVERT_HTTPS';
  }, [status, statusInfo]);

  return {
    status,
    isBuildLoading,
    isHttpsLoading,
    restartPolling: () => startPolling(true),
    startPolling,
  };
}
