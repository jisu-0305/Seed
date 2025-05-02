/* eslint-disable no-nested-ternary */
import styled from '@emotion/styled';
import { useState } from 'react';

import type { DeployStatusProps, DeployTabName } from '@/types/deploy';

export function DeployStatus({ tasksByTab }: DeployStatusProps) {
  const tabs = Object.keys(tasksByTab) as DeployTabName[];
  const [active, setActive] = useState<DeployTabName>(tabs[0]);

  return (
    <Container>
      <TabList>
        {tabs.map((t) => (
          <Tab key={t} active={t === active} onClick={() => setActive(t)}>
            {t}
          </Tab>
        ))}
      </TabList>

      <Table>
        <thead>
          <tr>
            <th>No</th>
            <th>작업</th>
            <th>실행 시간</th>
            <th>상태</th>
          </tr>
        </thead>
        <tbody>
          {tasksByTab[active].map(({ no, description, duration, status }) => (
            <tr key={no}>
              <td>{no}</td>
              <td>{description}</td>
              <td>{duration}</td>
              <td>
                <StatusBadge status={status}>{status}</StatusBadge>
              </td>
              <td>⋯</td>
            </tr>
          ))}
        </tbody>
      </Table>
    </Container>
  );
}

const Container = styled.div`
  margin-top: 3rem;
  background: #fff;
  border: 1px solid ${({ theme }) => theme.colors.LightGray1};
  border-radius: 1.2rem;
  overflow: hidden;
`;
const TabList = styled.div`
  display: flex;
`;
const Tab = styled.div<{ active: boolean }>`
  padding: 1rem 2rem;
  background: ${({ active, theme }) =>
    active ? '#fff' : theme.colors.LightGray1};
  border-bottom: ${({ active }) => (active ? 'none' : '1px solid #ddd')};
  cursor: pointer;
  ${({ theme }) => theme.fonts.Body4};
`;
const Table = styled.table`
  width: 100%;
  border-collapse: collapse;
  th,
  td {
    padding: 1rem;
    ${({ theme }) => theme.fonts.Body5};
  }
  th {
    text-align: left;
    background: ${({ theme }) => theme.colors.LightGray1};
  }
  tbody tr:hover {
    background: ${({ theme }) => theme.colors.LightGray2};
  }
`;
const StatusBadge = styled.span<{ status: string }>`
  padding: 0.25rem 0.75rem;
  border-radius: 1rem;
  background: ${({ status, theme }) =>
    status === 'Complete'
      ? theme.colors.Blue0
      : status === 'Fail'
        ? theme.colors.Red0
        : status === 'In Progress'
          ? theme.colors.Purple1
          : 'transparent'};
  color: ${({ status, theme }) =>
    status === 'Complete'
      ? theme.colors.Blue1
      : status === 'Fail'
        ? theme.colors.Red1
        : status === 'In Progress'
          ? theme.colors.Purple2
          : theme.colors.Gray3};
`;
