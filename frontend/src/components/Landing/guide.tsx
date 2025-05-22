import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';
import React, { useRef, useState } from 'react';

import { useThemeStore } from '@/stores/themeStore';

export default function Guide() {
  const router = useRouter();
  const { mode } = useThemeStore();
  const guideRef = useRef<HTMLDivElement>(null);
  const [showDownIcon, setShowDownIcon] = useState(true);

  const scrollToGuide = () => {
    guideRef.current?.scrollIntoView({ behavior: 'smooth' });
    setTimeout(() => setShowDownIcon(false), 600);
  };

  if (mode === null) return null;

  return (
    <PageWrapper>
      <TopSection>
        <Header>
          <Logo src="/assets/cactus.png" alt="SEED Logo" />
          <TitleGroup>
            <LogoText
              src={
                mode === 'light'
                  ? '/assets/icons/ic_logoText.svg'
                  : '/assets/icons/ic_logoText_white.svg'
              }
              alt="logoText"
            />
            <SubTitle>배포가 어려운 당신을 위한 솔루션</SubTitle>
          </TitleGroup>
        </Header>

        <Content>
          <ContentText>
            환영합니다! SEED 서비스에 오신 것을 환영합니다.
          </ContentText>
          <ContentText>
            원클릭 자동 배포, HTTPS 적용 및 AI 코드 수정을 경험해보세요.
          </ContentText>
        </Content>
        <Button onClick={() => router.replace('/landing')}>
          서비스 바로가기
        </Button>
        {showDownIcon && (
          <IcIcon
            src="/assets/ic_down.png"
            alt="close icon"
            onClick={scrollToGuide}
          />
        )}
      </TopSection>

      <GuideSection ref={guideRef}>
        <GuideItem>
          <GuideImage src="/assets/ec2.png" alt="EC2 설정" />
          <GuideTextWrapper>
            <GuideTitle>01 EC2 설정</GuideTitle>
            <GuideDesc>
              원클릭 자동 배포 파이프라인 구축
              <br />
              .PEM, IP 등만 입력하면 설정 끝
              <br />
              DOCKER, JENKINS 자동 설치 및 초기 설정 <br />
              GITLAB API 연동을 통한 코드 PUSH 시 자동 빌드·배포 진행
            </GuideDesc>
          </GuideTextWrapper>
        </GuideItem>

        <GuideItem reverse>
          <GuideImage src="/assets/https.png" alt="HTTPS 설정" />
          <GuideTextWrapper reverse>
            <GuideTitle>02 HTTPS 설정</GuideTitle>
            <GuideDesc>
              원클릭 HTTPS/도메인 적용
              <br />
              NGINX 설치 및 리버스 프록시 자동화
              <br />
              DNS 이름으로 사이트 이동 가능
            </GuideDesc>
          </GuideTextWrapper>
        </GuideItem>

        <GuideItem>
          <GuideImage src="/assets/ai.png" alt="AI 보고서" />
          <GuideTextWrapper>
            <GuideTitle>03 AI 빌드 에러 수정 및 배포</GuideTitle>
            <GuideDesc>
              AI 빌드 에러 수정 및 배포
              <br />
              JENKINS 빌드 실패 로그 자동 수집
              <br />
              AI 모델 기반 분석으로 원인 파악
              <br />
              코드 자동 수정 및 보고서 생성
              <br />
              FIX 브랜치에서 재빌드 시도
              <br />
              성공 시 GITLAB MR 자동 생성
            </GuideDesc>
          </GuideTextWrapper>
        </GuideItem>
      </GuideSection>
    </PageWrapper>
  );
}

const PageWrapper = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 6rem;
  padding-bottom: 10rem;
`;

const TopSection = styled.section`
  width: 100vw;
  height: calc(100vh - 20rem);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;

const Header = styled.header`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
`;

const Logo = styled.img`
  width: 20rem;
  height: 20rem;
  object-fit: contain;
`;

const TitleGroup = styled.div`
  display: flex;
  flex-direction: column;
`;

const LogoText = styled.img`
  height: 10rem;

  padding-top: 0.2rem;
`;

const SubTitle = styled.h2`
  margin: 1.5rem 0 0;
  ${({ theme }) => theme.fonts.EnTitle1};
  color: ${({ theme }) => theme.colors.Main_Carrot};
`;

const Content = styled.div`
  padding: 0rem 10rem;
  margin-bottom: 5rem;
  margin-top: 5rem;
`;

const ContentText = styled.div`
  ${({ theme }) => theme.fonts.Head7};
  margin-top: 1rem;
`;

const Button = styled.button`
  display: flex;
  flex-direction: row;
  justify-content: space-around;
  align-items: center;

  padding: 1rem 4rem;
  border-radius: 2rem;
  min-width: 10rem;
  width: fit-content;

  ${({ theme }) => theme.fonts.Title3};
  color: ${({ theme }) => theme.colors.White};
  background-color: ${({ theme }) => theme.colors.Main_Carrot};

  &:hover {
    background-color: ${({ theme }) => theme.colors.Carrot2};
  }
`;

const IcIcon = styled.img`
  position: absolute;
  bottom: 2rem;
  width: 8rem;

  cursor: pointer;
`;

const GuideSection = styled.section`
  width: 100vw;
  padding-top: 10rem;
  display: flex;
  flex-direction: column;
  gap: 10rem;
`;

const GuideItem = styled.div<{ reverse?: boolean }>`
  display: flex;
  flex-direction: ${({ reverse }) => (reverse ? 'row-reverse' : 'row')};
  align-items: center;
  justify-content: center;
  gap: 6rem;
  padding: 0 8vw;

  @media (max-width: 1024px) {
    flex-direction: column;
    padding: 0 4vw;
  }
`;

const GuideImage = styled.img`
  flex: 1;
  max-width: 60rem;
  width: 100%;
  object-fit: contain;
  border-radius: 1.5rem;
`;

const GuideTextWrapper = styled.div<{ reverse?: boolean }>`
  flex: 1;
  max-width: 60rem;
  text-align: ${({ reverse }) => (reverse ? 'right' : 'left')};

  @media (max-width: 1024px) {
    text-align: center;
  }
`;

const GuideTitle = styled.h3`
  ${({ theme }) => theme.fonts.Title1};
  color: ${({ theme }) => theme.colors.Text};
  margin-bottom: 1.5rem;
`;

const GuideDesc = styled.p`
  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Text};
  line-height: 1.75;
  white-space: pre-line;
`;
