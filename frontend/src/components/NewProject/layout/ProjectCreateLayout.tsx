'use client';

import styled from '@emotion/styled';
import { usePathname, useRouter } from 'next/navigation';

import SmallButton from '@/components/Common/button/SmallButton';
import Header from '@/components/Common/Header';
import { useProjectInfoStore } from '@/stores/projectStore';
import { useThemeStore } from '@/stores/themeStore';
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
  const { mode } = useThemeStore();

  const { onNextValidate } = useProjectInfoStore();

  const handleNext = () => {
    if (onNextValidate()) {
      router.push(`${getUrlFromId(currentStep + 1)}`);
    } else {
      alert('모든 항목을 입력해주세요.');
    }
  };

  if (mode === null) return null;

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
                    src={`/assets/icons/ic_button_arrow_left_${mode}.svg`}
                    alt="arrow left"
                  />
                  이전
                </SmallButton>
                <SmallButton variant="next" onClick={handleNext}>
                  다음
                  <Icon
                    src={`/assets/icons/ic_button_arrow_right_${mode}.svg`}
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
  max-width: 100rem;

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

  min-height: 50rem;

  /* padding: 0 3rem 2rem; */

  border: 1px solid ${({ theme }) => theme.colors.BorderDefault};
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
