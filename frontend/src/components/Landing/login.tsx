import styled from '@emotion/styled';
import { useRouter } from 'next/navigation';

export default function Login() {
  const router = useRouter();
  const goBack = () => router.push('/');
  const handleGitlab = () => {
    window.location.href = 'http://localhost:8080/api/users/oauth/gitlab/login';
  };

  return (
    <Wrapper>
      <Left>
        <Content>
          <Back onClick={goBack}>
            <ArrowLeft src="/assets/icons/ic_back.svg" alt="뒤로가기" />
            메인으로 돌아가기
          </Back>
          <LogoTextSmall
            src="/assets/icons/ic_logoText.svg"
            alt="SEED text logo"
          />
          <Subtitle>씨앗 프로젝트에 오신 것을 환영합니다!</Subtitle>
          <GitlabButton onClick={handleGitlab}>
            <GitlabIcon src="/assets/icons/ic_gitlab.svg" alt="GitLab" />
            Continue with Gitlab
          </GitlabButton>
        </Content>

        <Footer>
          © 2025 SSAFY12. All Rights Reserved. Made with love by SEED!
        </Footer>
      </Left>

      <Right>
        <Cactus src="/assets/cactus.png" alt="Cactus" />
        <LogoText
          src="/assets/icons/ic_logoText_white.svg"
          alt="SEED text logo"
        />
        <Pitch>배포가 어려운 당신을 위한 솔루션</Pitch>
      </Right>
    </Wrapper>
  );
}

const Wrapper = styled.div`
  display: flex;
  height: 100%;
`;

// Left pane
const Left = styled.div`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 2rem 4rem;

  max-width: 70rem;
`;

const Back = styled.button`
  position: absolute;
  top: 5rem;
  /* left: 5rem; */

  display: flex;
  align-items: center;

  border: none;
  background: none;
  ${({ theme }) => theme.fonts.Body3};
`;

const ArrowLeft = styled.img`
  width: 1rem;
  margin-right: 1.5rem;
`;

const Content = styled.div`
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2rem;
`;

const LogoTextSmall = styled.img`
  width: 15rem;
  height: auto;
`;

const Subtitle = styled.p`
  ${({ theme }) => theme.fonts.Body1};
  color: ${({ theme }) => theme.colors.Gray3};
`;

// Gitlab 버튼
const GitlabButton = styled.button`
  display: flex;
  align-items: center;
  background: ${({ theme }) => theme.colors.Black1};
  color: ${({ theme }) => theme.colors.White};
  border: none;
  border-radius: 1.5rem;
  padding: 0.75rem 6rem;
  ${({ theme }) => theme.fonts.EnTitle2};

  &:hover {
    background: ${({ theme }) => theme.colors.Black};
  }
`;

const GitlabIcon = styled.img`
  width: 3rem;
  margin-right: 1rem;
`;

// Footer (Left)
const Footer = styled.footer`
  position: absolute;
  bottom: 1rem;
  font-size: 1rem;
`;

// Right pane (비주얼)
const Right = styled.div`
  width: 100%;
  flex: 1;
  background-color: ${({ theme }) => theme.colors.Black1};
  color: ${({ theme }) => theme.colors.White};
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rem;
  border-bottom-left-radius: 20rem;

  padding: 0 2rem;
`;

const Cactus = styled.img`
  width: 20rem;
`;

const LogoText = styled.img`
  width: 35rem;
  height: auto;
`;

const Pitch = styled.p`
  color: ${({ theme }) => theme.colors.Main_Carrot};
  ${({ theme }) => theme.fonts.Title2};

  white-space: nowrap;
`;
