import styled from '@emotion/styled';
import { useMemo } from 'react';

import { SERVER_STATUS_INFO } from '@/utils/getStatusMessage';

interface ServerStatusProps {
  status: string;
}

export default function ServerStatusBar({ status }: ServerStatusProps) {
  const statusInfo = useMemo(() => SERVER_STATUS_INFO[status ?? ''], [status]);

  if (!statusInfo) return null;

  const { message, progress, category } = statusInfo;

  return (
    <StatusWrapper>
      <MessageBanner>
        <BannerMessage>{message}</BannerMessage>
      </MessageBanner>
      <ProgressBar>
        <ProgressFill
          style={{ width: `${progress}%` }}
          //   style={{ width: '50%' }}
          data-category={category}
          //   data-category="build"
        />
      </ProgressBar>
    </StatusWrapper>
  );
}

const StatusWrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 2rem;
  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1.5rem;
`;

// 메시지 배너
const MessageBanner = styled.div`
  position: relative;
  display: flex;
  align-items: center;
`;

const BannerMessage = styled.div`
  ${({ theme }) => theme.fonts.Body2};
  color: ${({ theme }) => theme.colors.Text};
`;

// 진행 바
const ProgressBar = styled.div`
  width: 100%;
  height: 0.5rem;
  background-color: ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 5rem;
  overflow: hidden;
`;

const ProgressFill = styled.div`
  height: 100%;
  transition: width 0.4s ease;
  background-color: ${({ theme }) => theme.colors.Blue1};
  border-radius: 5rem;

  &[data-category='ai'] {
    background-color: ${({ theme }) => theme.colors.Main_Carrot};
  }
`;
