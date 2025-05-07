import styled from '@emotion/styled';
import { useState } from 'react';

import { dummyBuilds } from '@/assets/dummy/builds';
import { useThemeStore } from '@/stores/themeStore';

import { DeployTable } from './DeployTable';

export function BuildHistoryPanel() {
  const [selectedId, setSelectedId] = useState(dummyBuilds[0].id);
  const selected = dummyBuilds.find((b) => b.id === selectedId)!;

  const { mode } = useThemeStore();

  return (
    <Wrapper>
      <LeftPanel>
        {dummyBuilds.map((b) => (
          <BuildItem
            key={b.id}
            active={b.id === selectedId}
            onClick={() => setSelectedId(b.id)}
          >
            <Icon status={b.status}>
              <IcIcon src={`/assets/icons/ic_build_${mode}.svg`} alt="build" />
            </Icon>
            <Info>
              <Title>
                #{b.id} {b.title}
              </Title>
              <Meta>
                {b.date} • {b.time}
              </Meta>
            </Info>
          </BuildItem>
        ))}
      </LeftPanel>

      <RightPanel>
        <DeployTable tasks={selected.tasks} />
        <LinkButton href={selected.link} target="_blank">
          GitLab 확인하기 →
        </LinkButton>
      </RightPanel>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  display: flex;
  max-height: 35rem;
  overflow: hidden;
`;

const LeftPanel = styled.div`
  width: 220px;
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
    status === 'SUCCESS' ? theme.colors.Text : theme.colors.Red2};

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

const LinkButton = styled.a`
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
