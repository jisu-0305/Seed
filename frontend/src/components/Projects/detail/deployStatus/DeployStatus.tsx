import styled from '@emotion/styled';

import { useThemeStore } from '@/stores/themeStore';
import type { DeployStatusProps, DeployTabName } from '@/types/deploy';

import { BuildHistoryPanel } from './BuildHistoryPanel';
import { DeployTable } from './DeployTable';

export function DeployStatus({
  projectId,
  buildNumber,
  tasksByTab,
  selectedTab,
  onTabChange,
}: DeployStatusProps) {
  const tabs = Object.keys(tasksByTab) as DeployTabName[];
  const { mode } = useThemeStore();
  const pid = Number(projectId);

  if (mode === null) return null;

  return (
    <Container>
      <TabList>
        {tabs.map((t) => (
          <Tab
            key={t}
            active={t === selectedTab}
            mode={mode}
            onClick={() => onTabChange(t)}
          >
            {t}
          </Tab>
        ))}
      </TabList>

      <ContentWrapper>
        {selectedTab === '빌드 기록' ? (
          <BuildHistoryPanel projectId={pid} />
        ) : (
          <DeployTable
            projectId={pid}
            buildNumber={buildNumber}
            tasks={tasksByTab[selectedTab]}
            selectedTab={selectedTab}
          />
        )}
      </ContentWrapper>
    </Container>
  );
}

const Container = styled.div`
  margin-top: 1rem;
  border-radius: 1rem;
`;

const TabList = styled.div`
  display: flex;
  justify-content: flex-end;
  position: relative;
  background: transparent;
`;

const Tab = styled.div<{ active: boolean; mode: string }>`
  position: relative;
  padding: 0.6rem 2rem;
  border: 1px solid ${({ theme }) => theme.colors.Gray0};
  border-bottom: none;
  border-radius: 1rem 1rem 0 0;

  background: ${({ active, theme }) =>
    active ? theme.colors.DetailBorder2 : theme.colors.Background};
  color: ${({ active, mode, theme }) =>
    active
      ? theme.colors.White
      : mode === 'light'
        ? theme.colors.Black
        : theme.colors.White};
  ${({ theme }) => theme.fonts.Body1};

  z-index: ${({ active }) => (active ? 2 : 1)};
  cursor: pointer;
`;

const ContentWrapper = styled.div`
  border: 1px solid ${({ theme }) => theme.colors.Gray0};
  border-radius: 1rem 0 1rem 1rem;
  max-height: 35rem;
  min-height: 35rem;

  overflow-y: auto;
  /* Firefox */
  scrollbar-width: thin;
  scrollbar-color: ${({ theme }) => `${theme.colors.Gray0} transparent`};

  /* WebKit (Chrome, Safari, Edge) */
  &::-webkit-scrollbar {
    width: 8px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: ${({ theme }) => theme.colors.Gray3};
    border-radius: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }
`;
