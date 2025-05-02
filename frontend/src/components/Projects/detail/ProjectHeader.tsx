import styled from '@emotion/styled';

interface ProjectHeaderProps {
  emojiSrc: string;
  https: boolean;
  lastBuildStatus: 'SUCCESS' | 'FAILURE' | null;
  lastBuildTime: string;
}

export function ProjectHeader({
  emojiSrc,
  https,
  lastBuildStatus,
  lastBuildTime,
}: ProjectHeaderProps) {
  return (
    <Wrapper>
      <Right>
        <Emoji src={emojiSrc} alt="project" />
        <StatusItem>
          자동배포{' '}
          <Icon src="/assets/icons/ic_autoDeploy.svg" alt="autoDeploy" />
        </StatusItem>
        <StatusItem>
          HTTPS{' '}
          <Icon
            src={`/assets/icons/ic_https_${https}.svg`}
            alt={`https_${https}`}
          />
        </StatusItem>
        <StatusItem>
          최근 빌드{' '}
          <Icon
            src={`/assets/icons/ic_${
              lastBuildStatus === 'SUCCESS' ? 'build_success' : 'build_fail'
            }.svg`}
            alt={lastBuildStatus === 'SUCCESS' ? 'build_success' : 'build_fail'}
          />
        </StatusItem>
        <Time>{lastBuildTime}</Time>
      </Right>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: ${({ theme }) => theme.colors.Blue4};
  padding: 2rem 3rem;
  border-radius: 1.6rem;
`;

const Right = styled.div`
  display: flex;
  align-items: center;
  gap: 2rem;
`;

const Emoji = styled.img`
  width: 3rem;
  height: 3rem;
`;

const StatusItem = styled.div`
  display: flex;
  align-items: center;
  gap: 0.5rem;
  ${({ theme }) => theme.fonts.Body4};
`;

const Time = styled.div`
  ${({ theme }) => theme.fonts.Body5};
`;

const Icon = styled.img`
  width: 1.5rem;
  height: 1.5rem;
  object-fit: contain;
`;
