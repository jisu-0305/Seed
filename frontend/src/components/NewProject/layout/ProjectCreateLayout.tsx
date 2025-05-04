'use client';

import styled from '@emotion/styled';
import { usePathname, useRouter } from 'next/navigation';

import SmallButton from '@/components/Common/button/SmallButton';
import Header from '@/components/Common/Header';
import { getIdFromUrl, getUrlFromId } from '@/utils/getProjectStep';

import StepSidebar from './StepSidebar';
import TopNavigation from './TopNavigation';

export default function ProjectCreateLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathName = usePathname();
  const key = pathName.split('/').pop() || '';
  const currentStep = getIdFromUrl(key);

  return (
    <>
      <Header title="새 프로젝트" />
      {currentStep !== 5 ? (
        <Wrapper>
          <TopNavigation currentStep={currentStep} />

          <Content>
            <Main>{children}</Main>
            <SideBarWrapper>
              <StepSidebar />

              <StButtonWrapper>
                <SmallButton
                  variant="cancel"
                  disabled={currentStep === 1}
                  onClick={() => {
                    router.push(`${getUrlFromId(currentStep - 1)}`);
                  }}
                >
                  <Icon
                    src="/assets/icons/ic_button_arrow_left.svg"
                    alt="arrow left"
                  />
                  이전
                </SmallButton>
                <SmallButton
                  variant="next"
                  onClick={() => {
                    router.push(`${getUrlFromId(currentStep + 1)}`);
                  }}
                >
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
      ) : (
        <Wrapper>{children}</Wrapper>
      )}
    </>
  );
}

const Wrapper = styled.div`
  max-width: 120rem;

  display: flex;
  flex-direction: column;

  margin: 0 auto;
  padding: 2rem;
  padding-top: 8rem;
`;

const Content = styled.div`
  display: flex;
  flex: 1;
  gap: 2rem;
`;

const Main = styled.main`
  flex: 6;
  display: flex;
  align-items: center;

  /* padding: 0 3rem 2rem; */

  border: 1px solid ${({ theme }) => theme.colors.LightGray1};
  border-radius: 1.5rem;
`;

const SideBarWrapper = styled.aside`
  flex: 4;
`;

const StButtonWrapper = styled.aside`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 2rem;

  padding-top: 3rem;
`;

const Icon = styled.img``;
