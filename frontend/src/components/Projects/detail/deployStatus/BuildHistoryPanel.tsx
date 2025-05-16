import styled from '@emotion/styled';
import {
  InfiniteData,
  useInfiniteQuery,
  useQuery,
} from '@tanstack/react-query';
import { useEffect, useState } from 'react';

import {
  BuildListResponse,
  BuildSummary,
  fetchBuildDetail,
  fetchBuildLogs,
  fetchBuilds,
} from '@/apis/build';
import { LoadingSpinner } from '@/components/Common/LoadingSpinner';
import ModalWrapper from '@/components/Common/Modal/ModalWrapper';
import SmallModal from '@/components/Common/Modal/SmallModal';
import { useModal } from '@/hooks/Common';
import { useThemeStore } from '@/stores/themeStore';
import type { Task } from '@/types/task';

import { DeployTable } from './DeployTable';

interface BuildHistoryPanelProps {
  projectId: number;
  selectedTab: string;
}

export function BuildHistoryPanel({
  projectId,
  selectedTab,
}: BuildHistoryPanelProps) {
  const { mode } = useThemeStore();
  const [selectedBuild, setSelectedBuild] = useState<number | null>(null);

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isLoading: loadingBuilds,
    isError: buildsError,
  } = useInfiniteQuery<
    BuildListResponse,
    Error,
    InfiniteData<BuildListResponse>,
    ['builds', number],
    number
  >({
    queryKey: ['builds', projectId],
    queryFn: ({ pageParam = 0 }) => fetchBuilds(projectId, pageParam),
    getNextPageParam: (lastPage) =>
      lastPage.hasNext ? lastPage.nextStart : undefined,
    initialPageParam: 0,
    staleTime: 1000 * 60 * 5,
  });

  const allBuilds = data?.pages.flatMap((page) => page.builds) ?? [];

  useEffect(() => {
    if (allBuilds.length > 0 && selectedBuild === null) {
      setSelectedBuild(allBuilds[0].buildNumber);
    }
  }, [allBuilds]);

  const onScroll: React.UIEventHandler<HTMLDivElement> = (e) => {
    const { scrollTop, scrollHeight, clientHeight } = e.currentTarget;
    if (
      scrollHeight - scrollTop - clientHeight < 100 &&
      hasNextPage &&
      !isFetchingNextPage
    ) {
      fetchNextPage();
    }
  };

  // 3) 선택된 빌드 상세(스텝) 조회
  const {
    data: tasks = [],
    isLoading: loadingTasks,
    error: tasksError,
  } = useQuery<Task[], Error>({
    queryKey: ['buildDetail', projectId, selectedBuild],
    queryFn: () => fetchBuildDetail(projectId, selectedBuild!),
    enabled: selectedBuild !== null,
    staleTime: 1000 * 60 * 5,
  });

  // 로그 모달 상태 관리
  const logModal = useModal();
  const [logBuildName, setLogBuildName] = useState<string>('');
  const [logBuildNumber, setLogBuildNumber] = useState<number>(0);

  // 로그 내용 조회 (초기에는 disabled)
  const {
    data: logText,
    isLoading: loadingLog,
    error: logError,
  } = useQuery<string, Error>({
    queryKey: ['buildLog', projectId, logBuildNumber],
    queryFn: () => fetchBuildLogs(projectId, logBuildNumber),
    enabled: logModal.isShowing,
  });

  // 로그 버튼 클릭 시
  const handleShowLog = (b: BuildSummary) => {
    setLogBuildName(b.buildName);
    setLogBuildNumber(b.buildNumber);
    logModal.toggle();
  };

  if (mode === null) return null;
  if (loadingBuilds)
    return (
      <Wrapper>
        <LoadingSpinner />
      </Wrapper>
    );
  if (buildsError) return <NoDataText>아직 빌드기록이 없습니다</NoDataText>;

  return (
    <Wrapper>
      <LeftPanel onScroll={onScroll}>
        {allBuilds.map((b) => (
          <BuildItem
            key={b.buildNumber}
            active={b.buildNumber === selectedBuild}
            onClick={() => setSelectedBuild(b.buildNumber)}
          >
            <Icon status={b.status}>
              <IcIcon src={`/assets/icons/ic_build_${mode}.svg`} alt="build" />
            </Icon>
            <Info>
              <Title>
                #{b.buildNumber} {b.buildName}
              </Title>
              <Meta>
                {b.date} • {b.time}
              </Meta>
            </Info>
          </BuildItem>
        ))}
      </LeftPanel>

      <RightPanel>
        {loadingTasks ? (
          <LoadingSpinner />
        ) : tasksError ? (
          <div>빌드 상세 조회 실패: {tasksError.message}</div>
        ) : (
          <>
            <DeployTable
              projectId={projectId}
              buildNumber={selectedBuild}
              tasks={tasks}
              selectedTab={selectedTab}
            />
            <LinkButton
              onClick={() =>
                handleShowLog(
                  allBuilds.find((b) => b.buildNumber === selectedBuild)!,
                )
              }
            >
              콘솔 로그 →
            </LinkButton>
          </>
        )}
      </RightPanel>

      <ModalWrapper isShowing={logModal.isShowing}>
        <SmallModal
          title={`빌드 로그 #${logBuildNumber} ${logBuildName}`}
          isShowing={logModal.isShowing}
          handleClose={logModal.toggle}
        >
          {loadingLog ? (
            <LoadingSpinner />
          ) : logError ? (
            <div>로그 불러오기 실패: {logError.message}</div>
          ) : (
            <LogContainer>
              <pre>{logText}</pre>
            </LogContainer>
          )}
        </SmallModal>
      </ModalWrapper>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  display: flex;
  height: 35rem;
  width: 100%;
  overflow: hidden;
`;

const LeftPanel = styled.div`
  width: 22rem;
  border-right: 1px solid ${({ theme }) => theme.colors.DetailBorder2};

  overflow-y: auto;

  /* 기본 상태에서 안 보이게 */
  &::-webkit-scrollbar {
    width: 8px;
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background-color: transparent;
    border-radius: 4px;
    transition: background-color 0.3s ease;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  /* 호버 시에만 thumb 표시 */
  &:hover::-webkit-scrollbar-thumb {
    background-color: ${({ theme }) => theme.colors.Gray3};
  }

  /* Firefox */
  scrollbar-width: none; /* 기본은 안 보이게 */
  &:hover {
    scrollbar-width: thin;
    scrollbar-color: ${({ theme }) => `${theme.colors.Gray3} transparent`};
  }
`;

const RightPanel = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;

  overflow-y: auto;
  border-right: 1px solid ${({ theme }) => theme.colors.DetailBorder2};
`;

const BuildItem = styled.div<{ active: boolean }>`
  display: flex;
  align-items: center;
  gap: 2rem;
  padding: 1rem 2rem;
  border-bottom: 1px solid ${({ theme }) => theme.colors.InputStroke};
  background: ${({ active, theme }) =>
    active ? theme.colors.InputStroke : 'transparent'};
  cursor: pointer;

  :hover {
    background: ${({ theme }) => theme.colors.DetailBorder1};
  }
`;

const Icon = styled.div<{ status: string }>`
  display: flex;
  justify-content: center;
  align-items: center;
  width: 3rem;
  height: 3rem;
  border-radius: 50%;
  background: ${({ status, theme }) =>
    status === 'FAIL' ? theme.colors.Red2 : theme.colors.Text};

  img {
    width: 2rem;
    height: 2rem;
  }
`;

const IcIcon = styled.img``;

const Info = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

const Title = styled.div`
  ${({ theme }) => theme.fonts.Body2};
`;

const Meta = styled.div`
  ${({ theme }) => theme.fonts.Body6};
  color: ${({ theme }) => theme.colors.Gray3};
`;

const LinkButton = styled.button`
  width: 11rem;
  margin: 1.5rem 0 1rem 1.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 9999px;
  border: 1px solid ${({ theme }) => theme.colors.Gray3};
  text-decoration: none;
  color: ${({ theme }) => theme.colors.Text};
  ${({ theme }) => theme.fonts.Body4};

  &:hover {
    background: ${({ theme }) => theme.colors.Text};
    color: ${({ theme }) => theme.colors.Background};
  }
`;

const LogContainer = styled.div`
  max-height: 40rem;
  overflow: auto;
  background: ${({ theme }) => theme.colors.Gray0};
  padding: 1rem;
  margin: 2rem;
  border-radius: 0.5rem;

  pre {
    white-space: pre-wrap;
    word-break: break-all;
    ${({ theme }) => theme.fonts.Body4};
    color: ${({ theme }) => theme.colors.White};
  }
`;

const NoDataText = styled.div`
  color: ${({ theme }) => theme.colors.Gray3};
  ${({ theme }) => theme.fonts.Body2};
  text-align: center;
  padding: 2rem;
`;
