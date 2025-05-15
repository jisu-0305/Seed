import styled from '@emotion/styled';

import { useThemeStore } from '@/stores/themeStore';

interface ProjectHeaderProps {
  emojiSrc: string;
  https: boolean;
  deploy: boolean;
  buildStatus: 'SUCCESS' | 'FAILURE' | null;
  lastBuildAt: string;
}

export function ProjectHeader({
  emojiSrc,
  https,
  deploy,
  buildStatus,
  lastBuildAt,
}: ProjectHeaderProps) {
  const { mode } = useThemeStore();

  if (mode === null) return null;

  return (
    <Wrapper>
      <Emoji src={emojiSrc} alt="project" />
      <StatusRow>
        <StatusGroup>
          <StatusItem>
            <Label>최근 빌드</Label>
            <Icon
              src={`/assets/icons/ic_${
                buildStatus === 'SUCCESS' ? 'build_success' : 'build_fail'
              }.svg`}
              alt="build_status"
            />
          </StatusItem>
          <Time>{lastBuildAt}</Time>
        </StatusGroup>

        <StatusGroup>
          <StatusItem>
            <Label>HTTPS</Label>
            <Icon
              src={`/assets/icons/ic_https_${https}_${mode}.svg`}
              alt={`https_${https}`}
            />
          </StatusItem>
          <StatusItem>
            <Label>자동배포</Label>
            <Icon
              src={`/assets/icons/ic_autoDeploy_${deploy}_${mode}.svg`}
              alt="auto-deploy"
            />
          </StatusItem>
        </StatusGroup>
      </StatusRow>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 25rem;
  height: 25rem;
  padding: 1rem 2rem;
  border: 0.15rem solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1.5rem;
`;

const Emoji = styled.img`
  width: 17rem;
  height: 17rem;
  margin-bottom: 1.5rem;
`;

const StatusRow = styled.div`
  display: flex;
  justify-content: center;
  width: 100%;
  gap: 3rem;
`;

const StatusGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
`;

const StatusItem = styled.div`
  display: flex;
  align-items: center;
  gap: 1rem;
  ${({ theme }) => theme.fonts.Body4};
`;

const Label = styled.div`
  min-width: 5rem;
  ${({ theme }) => theme.fonts.EnBody1};
`;

const Time = styled.div`
  ${({ theme }) => theme.fonts.EnBody2};
  color: ${({ theme }) => theme.colors.DetailText};
`;

const Icon = styled.img`
  width: 2rem;
  height: 2rem;
`;
