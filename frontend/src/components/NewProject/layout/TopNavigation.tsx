import styled from '@emotion/styled';

interface Props {
  currentStep: number; // 1 ~ 4
  stepLabels?: string[];
}

const ProjectCreateHeader = ({
  currentStep,
  stepLabels = ['기본 정보', '서버 접속정보', '어플리케이션 정보', '환경설정'],
}: Props) => {
  const progressPercent = (currentStep / stepLabels.length) * 100;

  return (
    <Wrapper>
      <Title>프로젝트 정보 입력</Title>
      <ProgressWrapper>
        <StepLabel>{`${currentStep}. ${stepLabels[currentStep - 1]}`}</StepLabel>
        <ProgressBar>
          <Progress style={{ width: `${progressPercent}%` }} />
        </ProgressBar>
      </ProgressWrapper>
    </Wrapper>
  );
};

export default ProjectCreateHeader;

const Wrapper = styled.div``;

const ProgressWrapper = styled.div`
  padding: 2rem 3rem;
  margin-bottom: 2rem;

  border: 1px solid ${({ theme }) => theme.colors.LightGray1};
  border-radius: 1.5rem;
`;

const Title = styled.h2`
  margin-bottom: 2rem;

  ${({ theme }) => theme.fonts.Head0};
`;

const StepLabel = styled.div`
  padding-bottom: 1.3rem;

  ${({ theme }) => theme.fonts.Head3};
`;

const ProgressBar = styled.div`
  height: 1rem;

  background: ${({ theme }) => theme.colors.LightGray1};
  border-radius: 2rem;
`;

const Progress = styled.div`
  height: 100%;

  background: ${({ theme }) => theme.colors.Black};
  border-radius: 2rem;
`;
