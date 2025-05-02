'use client';

import styled from '@emotion/styled';

import SmallButton from '@/components/Common/buttons/SmallButton';
import Header from '@/components/Common/Header';
import { StepStatus } from '@/types/project';

import StepSidebar from './StepSidebar';
import TopNavigation from './TopNavigation';

export default function ProjectCreateLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const stepStatus: StepStatus = {
    gitlab: {
      repo: 'S12P31A206',
      structure: '모노',
      directory: true,
    },
    server: {
      ip: '255.127.39.0',
      pem: true,
    },
    app: ['React', 'Spring', 'fastapi'],
    env: true,
  };

  return (
    <>
      <Header title="새 프로젝트" />
      <Wrapper>
        <TopNavigation currentStep={1} />
        <Content>
          <Main>{children}</Main>
          <SideBarWrapper>
            <StepSidebar status={stepStatus} />

            <StButtonWrapper>
              <SmallButton variant="cancel" disabled>
                <Icon
                  src="/assets/icons/ic_button_arrow_left.svg"
                  alt="arrow left"
                />
                이전
              </SmallButton>
              <SmallButton variant="next" disabled>
                다음
                <Icon
                  src="/assets/icons/ic_button_arrow_right.svg"
                  alt="arrow left"
                />
              </SmallButton>
            </StButtonWrapper>
          </SideBarWrapper>
        </Content>
      </Wrapper>
    </>
  );
}

const Wrapper = styled.div`
  max-width: 120rem;

  display: flex;
  flex-direction: column;

  margin: 0 auto;
  padding: 10rem 5rem;
`;

const Content = styled.div`
  display: flex;
  flex: 1;
  gap: 2rem;
`;

const Main = styled.main`
  min-height: 50rem;

  flex: 6;
  padding: 2rem 3rem;

  border: 1px solid ${({ theme }) => theme.colors.LightGray1};
  border-radius: 1.5rem;
`;

const SideBarWrapper = styled.aside`
  flex: 4;
`;

const StButtonWrapper = styled.aside`
  display: flex;
  flex-direction: row;
  justify-content: space-evenly;
  align-items: center;

  padding-top: 3rem;
`;

const Icon = styled.img``;
