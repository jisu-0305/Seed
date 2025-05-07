import styled from '@emotion/styled';

import { getLabelfromId } from '@/utils/getProjectStep';

interface Props {
  currentStep: number; // 1 ~ 4
}

const ProjectCreateHeader = ({ currentStep }: Props) => {
  const progressPercent = (currentStep / 4) * 100;

  return (
    <Wrapper>
      <Title>프로젝트 정보 입력</Title>
      <ProgressWrapper>
        <StepLabel>{`${currentStep}. ${getLabelfromId(currentStep)}`}</StepLabel>
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

  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 1.5rem;
`;

const Title = styled.h2`
  margin-bottom: 2rem;

  ${({ theme }) => theme.fonts.Head0};
  font-size: 2.6rem;
`;

const StepLabel = styled.div`
  padding-bottom: 1.3rem;

  ${({ theme }) => theme.fonts.Head3};
`;

const ProgressBar = styled.div`
  height: 1rem;

  background: ${({ theme }) => theme.colors.BorderDefault};
  border-radius: 2rem;
`;

const Progress = styled.div`
  height: 100%;

  background: ${({ theme }) => theme.colors.Text};
  border-radius: 2rem;
`;
