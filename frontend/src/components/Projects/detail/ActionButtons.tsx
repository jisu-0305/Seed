/* eslint-disable no-nested-ternary */
import styled from '@emotion/styled';

export function ActionButtons() {
  return (
    <Wrapper>
      <MainActions>
        <Button variant="ai">
          <Icon src="/assets/icons/ic_ai_report_carrot.svg" alt="ai_report" />
          AI 보고서
        </Button>
        <Button variant="build">
          <Icon src="/assets/icons/ic_build.svg" alt="build_now" />
          지금 빌드
        </Button>
        <Button variant="https">
          <Icon src="/assets/icons/ic_https_true.svg" alt="https" />
          Https 설정
        </Button>
      </MainActions>
      <SubActions>
        <SmallButton>
          <Icon src="/assets/icons/ic_gitlab_white.svg" alt="gitlab" /> GitLab
        </SmallButton>
        <SmallButton>
          <Icon src="/assets/icons/ic_edit.svg" alt="edit" /> 정보수정
        </SmallButton>
        <SmallButton>
          <Icon src="/assets/icons/ic_team.svg" alt="team" /> 팀원 관리
        </SmallButton>
      </SubActions>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1rem;
`;

const MainActions = styled.div`
  display: flex;
  gap: 1rem;
`;

const SubActions = styled.div`
  display: flex;
  gap: 0.5rem;
`;

type Variant = 'ai' | 'build' | 'https';

const Button = styled.button<{ variant: Variant }>`
  padding: 1rem 2rem;
  border: none;
  border-radius: 0.8rem;
  background: ${({ variant, theme }) =>
    variant === 'ai'
      ? theme.colors.Carrot2
      : variant === 'build'
        ? theme.colors.Blue3
        : theme.colors.Blue4};
  color: ${({ theme }) => theme.colors.Black};
  display: flex;
  align-items: center;
  gap: 0.5rem;
  ${({ theme }) => theme.fonts.Body4};
`;

const SmallButton = styled.button`
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 0.8rem;
  background: #000;
  color: #fff;
  font-size: 0.875rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  ${({ theme }) => theme.fonts.Body4};
`;

const Icon = styled.img`
  width: 1.5rem;
`;
