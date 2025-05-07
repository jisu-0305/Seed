/* eslint-disable no-nested-ternary */
import styled from '@emotion/styled';

import { Task } from '@/types/task';

export function DeployTable({ tasks }: { tasks: Task[] }) {
  return (
    <TableWrapper>
      <Table>
        <thead>
          <tr>
            <th>No</th>
            <th>작업</th>
            <th>실행 시간</th>
            <th>상태</th>
            <th>로그</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map(({ no, description, duration, status }) => (
            <tr key={no}>
              <td>{no}</td>
              <td>{description}</td>
              <td>{duration}</td>
              <td>
                <StatusBadge status={status}>{status}</StatusBadge>
              </td>
              <td>
                <Icon
                  src="/assets/icons/ic_more.svg"
                  alt="log"
                  onClick={() => {
                    console.log('젠킨스 로그 보여줄거임');
                  }}
                />
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    </TableWrapper>
  );
}

const TableWrapper = styled.div`
  overflow-y: auto;
  /* Firefox */
  scrollbar-width: thin;
  scrollbar-color: ${({ theme }) => `${theme.colors.Text} transparent`};

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

const Table = styled.table`
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;

  th,
  td {
    padding: 1.2rem 1.5rem;
    text-align: left;
    vertical-align: middle;
    ${({ theme }) => theme.fonts.Body1};
  }

  th {
    color: ${({ theme }) => theme.colors.Gray3};
    ${({ theme }) => theme.fonts.EnBody2};
    border-bottom: 1px solid ${({ theme }) => theme.colors.LightGray2};
  }

  th:last-child {
    text-align: center;
    vertical-align: middle;
  }

  td:last-child {
    text-align: center;
    vertical-align: middle;
  }

  tbody tr:hover {
    background-color: ${({ theme }) => theme.colors.BuildHover};
  }
`;

const StatusBadge = styled.span<{ status: string }>`
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.4rem 1.5rem;
  border-radius: 1.2rem;
  width: fit-content;

  ${({ theme }) => theme.fonts.EnBody2};
  color: ${({ theme }) => theme.colors.White};

  background: ${({ status, theme }) =>
    status === 'Complete'
      ? theme.colors.Blue2
      : status === 'Fail'
        ? theme.colors.Red3
        : theme.colors.Purple3};

  &::before {
    content: '●';
    font-size: 0.8rem;
    color: ${({ status, theme }) =>
      status === 'Complete'
        ? theme.colors.Blue1
        : status === 'Fail'
          ? theme.colors.Red2
          : theme.colors.Purple1};
  }
`;

const Icon = styled.img`
  width: 2rem;
  cursor: pointer;
`;
