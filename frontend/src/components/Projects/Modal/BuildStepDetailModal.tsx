// src/components/Common/BuildStepDetailModal.tsx
import styled from '@emotion/styled';
import { useQuery } from '@tanstack/react-query';
import React from 'react';

import { fetchStepLog } from '@/apis/build';
import { LoadingSpinner } from '@/components/Common/LoadingSpinner';
import SmallModal from '@/components/Common/Modal/SmallModal';
import { EchoList } from '@/types/task';

interface BuildStepDetailModalProps {
  isShowing: boolean;
  handleClose: () => void;
  projectId: number;
  buildNumber: number;
  stepNumber: number;
  stepName: string;
  echoList?: EchoList[];
}

export const BuildStepDetailModal: React.FC<BuildStepDetailModalProps> = ({
  isShowing,
  handleClose,
  projectId,
  buildNumber,
  stepNumber,
  stepName,
  echoList,
}) => {
  const {
    data: logText,
    isLoading,
    error,
  } = useQuery<string, Error>({
    queryKey: ['stepLog', projectId, buildNumber, stepNumber],
    queryFn: () => fetchStepLog(projectId, buildNumber, stepNumber),
    enabled: isShowing,
  });

  return (
    <SmallModal
      title={`#${buildNumber} - [Step ${stepNumber}] ${stepName}`}
      isShowing={isShowing}
      handleClose={handleClose}
    >
      {echoList && echoList.length > 0 && (
        <EchoWrapper>
          <EchoHeader>세부 정보:</EchoHeader>
          <EchoListContainer>
            {echoList.map((e) => (
              <EchoItem key={e.echoNumber}>
                <strong>{e.echoNumber}.</strong> {e.echoContent}{' '}
              </EchoItem>
            ))}
          </EchoListContainer>
        </EchoWrapper>
      )}
      <Content>
        {isLoading ? (
          <LoadingSpinner />
        ) : error ? (
          <ErrorMsg>로그 불러오기 실패: {error.message}</ErrorMsg>
        ) : (
          <LogPre>{logText}</LogPre>
        )}
      </Content>
    </SmallModal>
  );
};

const Content = styled.div`
  max-height: 30rem;
  overflow: auto;
  background: ${({ theme }) => theme.colors.Gray0};
  padding: 1rem;
  margin: 2rem;
  border-radius: 0.5rem;
`;

const LogPre = styled.pre`
  white-space: pre-wrap;
  word-break: break-all;
  ${({ theme }) => theme.fonts.Body4};
  color: ${({ theme }) => theme.colors.White};
`;

const ErrorMsg = styled.div`
  ${({ theme }) => theme.fonts.Body3};
  color: ${({ theme }) => theme.colors.Red2};
`;

const EchoWrapper = styled.div`
  padding: 1rem 3rem;
`;

const EchoHeader = styled.div`
  ${({ theme }) => theme.fonts.Title4};
  color: ${({ theme }) => theme.colors.Black};
`;

const EchoListContainer = styled.ul`
  margin-top: 1.5rem;
  padding-left: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
`;

const EchoItem = styled.li`
  ${({ theme }) => theme.fonts.Body2};
`;
