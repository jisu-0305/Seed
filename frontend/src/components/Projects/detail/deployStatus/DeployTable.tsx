import styled from '@emotion/styled';
import { useState } from 'react';

import ModalWrapper from '@/components/Common/Modal/ModalWrapper';
import { useModal } from '@/hooks/Common';
import { EchoList, Task } from '@/types/task';

import { BuildStepDetailModal } from '../../Modal/BuildStepDetailModal';

interface DeployTableProps {
  projectId: number;
  buildNumber: number | null;
  tasks: Task[];
  selectedTab: string;
}

export function DeployTable({
  projectId,
  buildNumber,
  tasks,
  selectedTab,
}: DeployTableProps) {
  const stepModal = useModal();
  const [currentStep, setCurrentStep] = useState<{
    number: number;
    name: string;
    echoList?: EchoList[];
  } | null>(null);

  const handleStepClick = (
    stepNumber: number,
    stepName: string,
    echoList?: EchoList[],
  ) => {
    if (buildNumber == null) return;

    setCurrentStep({ number: stepNumber, name: stepName, echoList });
    stepModal.toggle();
  };

  return (
    <TableWrapper>
      <Table>
        <thead>
          <tr>
            <th>No</th>
            <th>작업</th>
            <th>실행 시간</th>
            <th>상태</th>
            <th>{selectedTab === 'Https 세팅' ? null : '로그'}</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map(({ stepNumber, stepName, duration, status, echoList }) => (
            <tr key={stepNumber}>
              <td>{stepNumber}</td>
              <td>{stepName}</td>
              <td>{duration}</td>
              <td>
                <StatusBadge status={status}>{status}</StatusBadge>
              </td>
              {selectedTab === 'Https 세팅' ? null : (
                <td>
                  <Icon
                    src="/assets/icons/ic_more.svg"
                    alt="log"
                    onClick={() =>
                      handleStepClick(stepNumber, stepName, echoList)
                    }
                  />
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </Table>
      {currentStep && (
        <ModalWrapper isShowing={stepModal.isShowing}>
          <BuildStepDetailModal
            isShowing={stepModal.isShowing}
            handleClose={stepModal.toggle}
            projectId={projectId}
            buildNumber={buildNumber!}
            stepNumber={currentStep.number}
            stepName={currentStep.name}
            echoList={currentStep.echoList}
          />
        </ModalWrapper>
      )}
    </TableWrapper>
  );
}

const TableWrapper = styled.div`
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: ${({ theme }) => `${theme.colors.Gray0} transparent`};
`;

const Table = styled.table`
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;

  thead th {
    position: sticky;
    top: 0;
    background: ${({ theme }) => theme.colors.Background}; /* 헤더 배경색 */
    z-index: 2;
    border-bottom: 1px solid ${({ theme }) => theme.colors.InputStroke};
  }

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
    border-bottom: 1px solid ${({ theme }) => theme.colors.InputStroke};
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
    status === 'SUCCESS'
      ? theme.colors.Blue2
      : status === 'FAIL'
        ? theme.colors.Red3
        : status === 'FAILED'
          ? theme.colors.Red3
          : theme.colors.Purple3};

  &::before {
    content: '●';
    font-size: 0.8rem;
    color: ${({ status, theme }) =>
      status === 'SUCCESS'
        ? theme.colors.Blue1
        : status === 'FAIL'
          ? theme.colors.Red2
          : status === 'FAILED'
            ? theme.colors.Red2
            : theme.colors.Purple1};
  }
`;

const Icon = styled.img`
  width: 2rem;
  cursor: pointer;
`;
